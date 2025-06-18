package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.error.ForbiddenException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = ShareItApp.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@example.com"));

        item = new Item();
        item.setName("Camera");
        item.setDescription("HD camera");
        item.setAvailable(true);
        item.setOwnerId(owner.getId());
        item = itemRepository.save(item);
    }

    @Test
    void createBooking_andFetchById_Succeeds() {
        BookingRequestDto dto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        BookingDto created = bookingService.create(booker.getId(), dto);
        assertNotNull(created.getId());
        assertEquals(booker.getId(), created.getBooker().getId());
        assertEquals(item.getId(), created.getItem().getId());
        assertEquals(BookingStatus.WAITING, created.getStatus());

        BookingDto fetchedByBooker = bookingService.getById(booker.getId(), created.getId());
        assertEquals(created.getId(), fetchedByBooker.getId());

        BookingDto fetchedByOwner = bookingService.getById(owner.getId(), created.getId());
        assertEquals(created.getId(), fetchedByOwner.getId());
    }

    @Test
    void approveBooking_ChangesStatus() {
        BookingDto created = bookingService.create(booker.getId(), new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        ));

        BookingDto approved = bookingService.approveBooking(owner.getId(), created.getId(), true);
        assertEquals(BookingStatus.APPROVED, approved.getStatus());

        assertThrows(RuntimeException.class,
                () -> bookingService.approveBooking(owner.getId(), created.getId(), true));
    }

    @Test
    void getBooking_ForUnauthorizedUser_Throws() {
        BookingDto created = bookingService.create(booker.getId(), new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        ));
        User stranger = userRepository.save(new User(null, "Stranger", "stranger@example.com"));

        assertThrows(ForbiddenException.class,
                () -> bookingService.getById(stranger.getId(), created.getId()));
    }

    @Test
    void getAllOwnByBooker_InvalidPageParameters_Throws() {
        assertThrows(RuntimeException.class,
                () -> bookingService.getAllOwnByBooker(999L, "ALL", 0, 10));
        assertThrows(RuntimeException.class,
                () -> bookingService.getAllOwnByBooker(booker.getId(), "ALL", -1, 10));
        assertThrows(RuntimeException.class,
                () -> bookingService.getAllOwnByBooker(booker.getId(), "ALL", 0, 0));
    }
}
