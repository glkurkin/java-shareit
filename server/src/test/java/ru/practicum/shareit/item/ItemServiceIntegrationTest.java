package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User requester;

    @BeforeEach
    void setUp() {
        // чистим БД
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User(null, "Owner", "owner@mail.com"));
        requester = userRepository.save(new User(null, "Requester", "req@mail.com"));
    }

    @Test
    void createItemWithoutRequestSucceeds() {
        var dto = new ItemRequestDto();
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);

        ItemResponseDto resp = itemService.create(owner.getId(), dto);

        assertNotNull(resp.getId());
        assertEquals("Drill", resp.getName());
        assertTrue(resp.getAvailable());

        List<Item> all = itemRepository.findAll();
        assertEquals(1, all.size());
        assertNull(all.get(0).getRequest(), "У item.request должно быть null");
    }

    @Test
    void createItemWithNonexistentRequestThrows() {
        var dto = new ItemRequestDto();
        dto.setName("Saw");
        dto.setDescription("Wood saw");
        dto.setAvailable(true);
        dto.setRequestId(999L);

        assertThrows(NotFoundException.class,
                () -> itemService.create(owner.getId(), dto));
    }

    @Test
    void createItemWithRequestSucceeds() {
        ItemRequest req = new ItemRequest(null, "Need drill", LocalDateTime.now(), requester);
        req = itemRequestRepository.save(req);

        var dto = new ItemRequestDto();
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);
        dto.setRequestId(req.getId());

        ItemResponseDto resp = itemService.create(owner.getId(), dto);

        assertNotNull(resp.getId());
        Item saved = itemRepository.findById(resp.getId()).orElseThrow();
        assertNotNull(saved.getRequest(), "Запрос должен сохраниться");
        assertEquals(req.getId(), saved.getRequest().getId());
    }

    @Test
    void createItemWithBlankNameThrows() {
        var dto = new ItemRequestDto();
        dto.setName("   ");
        dto.setDescription("desc");
        dto.setAvailable(true);

        assertThrows(BadRequestException.class,
                () -> itemService.create(owner.getId(), dto));
    }
}
