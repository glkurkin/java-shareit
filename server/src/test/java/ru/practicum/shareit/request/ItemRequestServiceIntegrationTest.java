package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;
    @Autowired
    private ItemRequestRepository requestRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ItemRepository itemRepo;

    private User requester;
    private User owner;
    private ItemRequest savedRequest;

    @BeforeEach
    void setUp() {
        // очистим всё
        itemRepo.deleteAll();
        requestRepo.deleteAll();
        userRepo.deleteAll();

        requester = userRepo.save(new User(null, "Пётр", "peter@example.com"));
        owner = userRepo.save(new User(null, "Иван", "ivan@example.com"));

        // создаём у Петра запрос
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна отвертка");
        savedRequest = requestService.createRequest(requester.getId(), dto);
    }

    @Test
    void createRequest_ShouldPersistRequest() {
        assertNotNull(savedRequest.getId());
        assertEquals("Нужна отвертка", savedRequest.getDescription());
        assertEquals(requester.getId(), savedRequest.getRequestor().getId());
        assertNotNull(savedRequest.getCreated());
    }

    @Test
    void getRequestById_ShouldReturnWithAnswers() {
        // заведём у Ивана вещь по этому запросу
        Item item = new Item();
        item.setName("Отвёртка крестовая");
        item.setDescription("Новая");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(savedRequest);
        itemRepo.save(item);

        var dtoWithAnswers = requestService.getRequestById(requester.getId(), savedRequest.getId());

        assertEquals(savedRequest.getId(), dtoWithAnswers.getId());
        assertEquals(1, dtoWithAnswers.getItems().size());
        var it = dtoWithAnswers.getItems().get(0);
        assertEquals(item.getId(), it.getId());
        assertEquals(item.getName(), it.getName());
        assertEquals(owner.getId(), it.getOwnerId());
    }

    @Test
    void getOwnRequests_ShouldReturnOnlyRequester() {
        // добавим ещё чужой запрос
        User other = userRepo.save(new User(null, "Саша", "sasha@example.com"));
        requestService.createRequest(other.getId(), new ItemRequestDto(null, "Что-то ещё", LocalDateTime.now()));

        List<ItemRequest> own = requestService.getOwnRequests(requester.getId());

        assertEquals(1, own.size());
        assertEquals(requester.getId(), own.get(0).getRequestor().getId());
    }

    @Test
    void getAllRequests_ShouldReturnOthersPaginated() {
        // ещё один чужой запрос
        User other = userRepo.save(new User(null, "Саша", "sasha@example.com"));
        requestService.createRequest(other.getId(), new ItemRequestDto(null, "Нужна дрель", LocalDateTime.now()));

        // получаем все, пропуская первый(=запрос requester), size=10
        List<ItemRequest> list = requestService.getAllRequests(requester.getId(), 0, 10);

        // в списке — только запросы НЕ от requester
        assertTrue(list.stream().allMatch(r -> !r.getRequestor().getId().equals(requester.getId())));
        assertEquals(1, list.size());
    }
}
