package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestExcep;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingServiceTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemStorage itemStorage;
    @Autowired
    private UserStorage userStorage;
    private User owner;
    private User user1;
    private BookingRequestDto bookingRequestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("Иван");
        user1.setEmail("vanya@mail.ru");
        userStorage.save(user1);

        owner = new User();
        owner.setName("Владелец");
        owner.setEmail("owner@owner.ru");
        userStorage.save(owner);

        item = new Item();
        item.setName("Вещь для ДБ");
        item.setDescription("Описание вещи для ДБ");
        item.setAvailable(true);
        item.setOwner(owner);
        itemStorage.save(item);

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().minusDays(2));
        bookingRequestDto.setEnd(LocalDateTime.now().minusDays(1));
    }

    @Test
    @DisplayName("Добавление бронирования")
    void addBooking() {
        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        assertThat(bookingDto).isNotNull();
        assertThat(bookingDto.getItem().getName()).isEqualTo("Вещь для ДБ");
        assertThat(bookingDto.getBooker().getId()).isEqualTo(user1.getId());
    }

    @Test
    @DisplayName("Попытка забронировать недоступную вещь")
    void addBookingForUnavailableItem() {
        item.setAvailable(false);
        itemStorage.save(item);
        assertThatExceptionOfType(BadRequestExcep.class)
                .isThrownBy(() -> bookingService.addNewBooking(user1.getId(), bookingRequestDto))
                .withMessageContaining("Вещь недоступна для брони");
    }


    @Test
    @DisplayName("Подтверждение бронирования")
    void updateBooking() {
        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        BookingDto updatedDto = bookingService.updateBooking(owner.getId(), bookingDto.getId(), true);
        assertThat(updatedDto.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("Отклонение бронирования")
    void rejectBooking() {
        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        BookingDto updatedDto = bookingService.updateBooking(owner.getId(), bookingDto.getId(), false);

        assertThat(updatedDto.getStatus()).isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    @DisplayName("Обновление статуса бронирования не владельцем предмета")
    void updateBookingNotOwner() {
        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        assertThatExceptionOfType(BadRequestExcep.class)
                .isThrownBy(() -> bookingService.updateBooking(user1.getId(), bookingDto.getId(), true))
                .withMessageContaining("подтверждает только владелец предмета");
    }

    @Test
    @DisplayName("Обновление статуса бронирования не существующим пользователем")
    void updateBookingWithNullUser() {
        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        assertThatExceptionOfType(BadRequestExcep.class)
                .isThrownBy(() -> bookingService.updateBooking(123, bookingDto.getId(), true))
                .withMessageContaining("не найден");
    }

    @Test
    @DisplayName("Полечение бронирование пользователя")
    void getByUserId() {
        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        BookingDto bookingFromUser1 = bookingService.getByUserId(user1.getId(), bookingDto.getId());
        assertThat(bookingFromUser1).isNotNull();
        assertThat(bookingFromUser1.getBooker().getName()).isEqualTo(user1.getName());
        assertThat(bookingFromUser1.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    @DisplayName("Получение бронирования несуществующим пользователем")
    void getByUserIdWithInvalidUser() {
        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        assertThatExceptionOfType(BadRequestExcep.class)
                .isThrownBy(() -> bookingService.getByUserId(999, bookingDto.getId()))
                .withMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Получение несуществующего бронирования")
    void getByUserIdWithInvalidBooking() {
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> bookingService.getByUserId(user1.getId(), 999))
                .withMessageContaining("Бронирование не найдено");
    }

    @Test
    @DisplayName("Получение всех бронирований владельца с фильтром ALL")
    void getByOwnerIdWithAll() {
        BookingDto bookingDto1 = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        bookingService.updateBooking(owner.getId(), bookingDto1.getId(), true);

        BookingRequestDto bookingRequestDto2 = new BookingRequestDto();
        bookingRequestDto2.setItemId(item.getId());
        bookingRequestDto2.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto2.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto bookingDto2 = bookingService.addNewBooking(user1.getId(), bookingRequestDto2);

        List<BookingDto> bookingDtos = bookingService.getByOwnerId(owner.getId(), BookingState.ALL);

        assertThat(bookingDtos).hasSize(2);
        assertThat(bookingDtos).extracting(BookingDto::getStatus)
                .containsExactlyInAnyOrder(BookingStatus.APPROVED, BookingStatus.WAITING);
    }

    @Test
    @DisplayName("Получение текущих бронирований владельца")
    void getByOwnerIdWithCurrent() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().minusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(1));

        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        bookingService.updateBooking(owner.getId(), bookingDto.getId(), true);

        List<BookingDto> bookingDtos = bookingService.getByOwnerId(owner.getId(), BookingState.CURRENT);

        assertThat(bookingDtos).hasSize(1);
        assertThat(bookingDtos.get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("Получение прошедших бронирований владельца")
    void getByOwnerIdWithPast() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().minusDays(3));
        bookingRequestDto.setEnd(LocalDateTime.now().minusDays(2));

        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);
        bookingService.updateBooking(owner.getId(), bookingDto.getId(), true);

        List<BookingDto> bookingDtos = bookingService.getByOwnerId(owner.getId(), BookingState.PAST);

        assertThat(bookingDtos).hasSize(1);
        assertThat(bookingDtos.get(0).getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("Получение будущих бронирований владельца")
    void getByOwnerIdWithFuture() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto bookingDto = bookingService.addNewBooking(user1.getId(), bookingRequestDto);

        List<BookingDto> bookingDtos = bookingService.getByOwnerId(owner.getId(), BookingState.FUTURE);

        assertThat(bookingDtos).hasSize(1);
        assertThat(bookingDtos.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    @DisplayName("Обработка ошибки при получении бронирований несуществующего владельца")
    void getByOwnerIdWithInvalidOwner() {
        Integer invalidOwnerId = 999;

        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> bookingService.getByOwnerId(invalidOwnerId, BookingState.ALL))
                .withMessageContaining("не найден");
    }

    @Test
    @DisplayName("Получение бронирований со статусом WAITING")
    void getByOwnerIdWithWaiting() {
        BookingRequestDto waitingBookingRequest = new BookingRequestDto();
        waitingBookingRequest.setItemId(item.getId());
        waitingBookingRequest.setStart(LocalDateTime.now().plusDays(1));
        waitingBookingRequest.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.addNewBooking(user1.getId(), waitingBookingRequest);

        List<BookingDto> bookingDtos = bookingService.getByOwnerId(owner.getId(), BookingState.WAITING);

        assertThat(bookingDtos).hasSize(1);
        assertThat(bookingDtos.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    @DisplayName("Получение всех бронирований")
    void findAllBookings() {
        BookingRequestDto bookingRequestDto1 = new BookingRequestDto();
        bookingRequestDto1.setItemId(item.getId());
        bookingRequestDto1.setStart(LocalDateTime.now().minusDays(3));
        bookingRequestDto1.setEnd(LocalDateTime.now().minusDays(2));
        bookingService.addNewBooking(user1.getId(), bookingRequestDto1);

        BookingRequestDto bookingRequestDto2 = new BookingRequestDto();
        bookingRequestDto2.setItemId(item.getId());
        bookingRequestDto2.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto2.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.addNewBooking(user1.getId(), bookingRequestDto2);

        List<BookingDto> bookingDtos = bookingService.findAll();

        assertThat(bookingDtos).hasSize(2);
        assertThat(bookingDtos)
                .extracting(bookingDto -> bookingDto.getItem().getName())
                .containsOnly("Вещь для ДБ");
    }

    @Test
    @DisplayName("Получение всех бронирований при их отсутствии")
    void findAllWithNoBookings() {
        List<BookingDto> bookingDtos = bookingService.findAll();

        assertThat(bookingDtos).isEmpty();
    }
}
