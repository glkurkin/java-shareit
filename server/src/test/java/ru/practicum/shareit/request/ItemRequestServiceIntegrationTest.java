package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ShareItApp.class)
@ActiveProfiles("test")
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @BeforeEach
    void cleanUp() {
        requestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createRequest_shouldReturnDtoWithIdDescriptionAndTimestamp() {
        User user = new User(null, "Ivan", "ivan@example.com");
        user = userRepository.save(user);

        CreateItemRequestDto dto = new CreateItemRequestDto("Нужна дрель");
        ItemRequestResponseDto resp = requestService.createRequest(user.getId(), dto);

        assertNotNull(resp.getId(), "id нового запроса не должен быть null");
        assertEquals(dto.getDescription(), resp.getDescription(), "описание должно сохраниться");
        assertNotNull(resp.getCreated(), "дата/время создания не должно быть null");
    }

    @Test
    void getOwnRequests_shouldReturnListOfOwnRequests() {
        User user = userRepository.save(new User(null, "Anna", "anna@example.com"));
        requestService.createRequest(user.getId(), new CreateItemRequestDto("Нужен шуруповёрт"));

        List<ItemRequestResponseDto> own = requestService.getOwnRequests(user.getId());
        assertEquals(1, own.size(), "должен вернуться один собственный запрос");
        ItemRequestResponseDto r = own.get(0);
        assertEquals("Нужен шуруповёрт", r.getDescription());
    }

    @Test
    void getAllOtherRequests_shouldReturnRequestsOfOthers() {
        User u1 = userRepository.save(new User(null, "Petr", "petr@example.com"));
        User u2 = userRepository.save(new User(null, "Olga", "olga@example.com"));
        requestService.createRequest(u1.getId(), new CreateItemRequestDto("Ищу велосипед"));

        List<ItemRequestResponseDto> others = requestService.getAllOtherRequests(u2.getId(), 0, 10);
        assertEquals(1, others.size(), "должен вернуться один чужой запрос");
        assertEquals("Ищу велосипед", others.get(0).getDescription());
    }

    @Test
    void getRequestById_shouldReturnThatRequest() {
        User user = userRepository.save(new User(null, "Max", "max@example.com"));
        ItemRequestResponseDto created = requestService.createRequest(user.getId(), new CreateItemRequestDto("Нужны лыжи"));
        ItemRequestResponseDto found = requestService.getRequestById(user.getId(), created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getDescription(), found.getDescription());
    }
}