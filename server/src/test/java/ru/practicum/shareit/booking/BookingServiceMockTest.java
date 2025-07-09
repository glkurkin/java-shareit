package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceMockTest {
    @Mock
    private BookingStorage bookingStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private ItemStorage itemStorage;
    @Mock
    private BookingMapper bookingMapper;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private User user;
    private Item item;
    private BookingRequestDto bookingRequestDto;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Name");
        user.setEmail("email@email.ru");

        item = new Item();
        item.setId(1);
        item.setName("Item1");
        item.setDescription("Item1des");
        item.setOwner(user);
        item.setAvailable(true);

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(5));

        booking = new Booking();
        booking.setId(1);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());

        bookingDto = new BookingDto();
        bookingDto.setId(1);
        bookingDto.setBooker(new UserDto());
        bookingDto.getBooker().setId(user.getId());
        bookingDto.getBooker().setName(user.getName());
        bookingDto.setItem(new ItemDto());
        bookingDto.getItem().setId(item.getId());
        bookingDto.getItem().setName(item.getName());
    }

    @Test
    @DisplayName("Добавление нового бронирования")
    void addNewBooking() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(itemStorage.findById(1)).thenReturn(Optional.of(item));
        when(bookingMapper.requestToBooking(any(BookingRequestDto.class))).thenAnswer(invocationOnMock -> {
            BookingRequestDto bookingRequestDto1 = invocationOnMock.getArgument(0);
            Booking booking = new Booking();
            booking.setItem(item);
            booking.setBooker(user);
            booking.setStart(bookingRequestDto1.getStart());
            booking.setEnd(bookingRequestDto1.getEnd());
            return booking;
        });
        when(bookingStorage.save(any(Booking.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(bookingMapper.toBookingDto(any(Booking.class))).thenAnswer(invocationOnMock -> {
            Booking booking = invocationOnMock.getArgument(0);
            BookingDto bookingDto = new BookingDto();
            bookingDto.setId(booking.getId());
            bookingDto.setStart(booking.getStart());
            bookingDto.setEnd(booking.getEnd());
            bookingDto.setStatus(booking.getStatus());

            bookingDto.setBooker(new UserDto());
            bookingDto.getBooker().setId(booking.getBooker().getId());
            bookingDto.getBooker().setName(booking.getBooker().getName());

            bookingDto.setItem(new ItemDto());
            bookingDto.getItem().setId(booking.getItem().getId());
            bookingDto.getItem().setName(booking.getItem().getName());
            return bookingDto;
        });
        BookingDto bookingDto = bookingService.addNewBooking(1, bookingRequestDto);
        assertThat(bookingDto).isNotNull();
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookingDto.getBooker().getId()).isEqualTo(user.getId());
        assertThat(bookingDto.getItem().getId()).isEqualTo(item.getId());

        verify(userStorage, times(1)).findById(1);
        verify(itemStorage, times(2)).findById(1);
        verify(bookingStorage, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Обновление бронирования")
    void updateBooking() {
        User mockUser = new User();
        mockUser.setId(user.getId());
        mockUser.setName(user.getName());
        mockUser.setEmail(user.getEmail());

        Item mockItem = new Item();
        mockItem.setId(item.getId());
        mockItem.setName(item.getName());
        mockItem.setDescription(item.getDescription());
        mockItem.setAvailable(true);
        mockItem.setOwner(mockUser);


        Booking mockBooking = new Booking();
        mockBooking.setId(booking.getId());
        mockBooking.setStatus(booking.getStatus());
        mockBooking.setStart(LocalDateTime.now().minusDays(5));
        mockBooking.setEnd(LocalDateTime.now().plusDays(2));
        mockBooking.setBooker(mockUser);
        mockBooking.setItem(mockItem);

        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(bookingStorage.findById(1)).thenReturn(Optional.of(mockBooking));
        when(bookingMapper.toBookingDto(any(Booking.class))).thenAnswer(invocationOnMock -> {
            Booking booking = invocationOnMock.getArgument(0);
            BookingDto mockBookingDto = new BookingDto();
            mockBookingDto.setId(booking.getId());
            mockBookingDto.setStart(booking.getStart());
            mockBookingDto.setEnd(booking.getEnd());
            mockBookingDto.setStatus(booking.getStatus());

            UserDto userDto = new UserDto();
            userDto.setId(booking.getBooker().getId());
            userDto.setName(booking.getBooker().getName());
            mockBookingDto.setBooker(userDto);

            ItemDto itemDto = new ItemDto();
            itemDto.setId(booking.getItem().getId());
            itemDto.setName(booking.getItem().getName());
            mockBookingDto.setItem(itemDto);

            return mockBookingDto;
        });
        BookingDto updatedDto = bookingService.updateBooking(1, 1, true);
        assertThat(updatedDto).isNotNull();
        assertThat(updatedDto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(updatedDto.getBooker().getId()).isEqualTo(user.getId());

        verify(userStorage, times(1)).findById(1);
        verify(bookingStorage, times(1)).findById(1);
        verify(bookingStorage, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Получение бронирования по ИД юзера и ИД бронирования")
    void getBookingByUserId() {
        User mockUser = new User();
        mockUser.setId(user.getId());
        mockUser.setName(user.getName());
        mockUser.setEmail(user.getEmail());

        Item mockItem = new Item();
        mockItem.setId(item.getId());
        mockItem.setName(item.getName());
        mockItem.setDescription(item.getDescription());
        mockItem.setAvailable(true);
        mockItem.setOwner(mockUser);

        Booking mockBooking = new Booking();
        mockBooking.setId(booking.getId());
        mockBooking.setStatus(booking.getStatus());
        mockBooking.setStart(LocalDateTime.now().minusDays(5));
        mockBooking.setEnd(LocalDateTime.now().plusDays(2));
        mockBooking.setBooker(mockUser);
        mockBooking.setItem(mockItem);
        when(userStorage.findById(mockUser.getId())).thenReturn(Optional.of(user));
        when(bookingStorage.findById(mockBooking.getId())).thenReturn(Optional.of(mockBooking));
        when(bookingMapper.toBookingDto(any(Booking.class))).thenAnswer(invocationOnMock -> {
            Booking booking = invocationOnMock.getArgument(0);
            BookingDto mockBookingDto = new BookingDto();
            mockBookingDto.setId(booking.getId());
            mockBookingDto.setStart(booking.getStart());
            mockBookingDto.setEnd(booking.getEnd());
            mockBookingDto.setStatus(booking.getStatus());

            UserDto userDto = new UserDto();
            userDto.setId(booking.getBooker().getId());
            userDto.setName(booking.getBooker().getName());
            mockBookingDto.setBooker(userDto);

            ItemDto itemDto = new ItemDto();
            itemDto.setId(booking.getItem().getId());
            itemDto.setName(booking.getItem().getName());
            mockBookingDto.setItem(itemDto);

            return mockBookingDto;
        });

        BookingDto booking = bookingService.getByUserId(1, 1);
        assertThat(booking).isNotNull();
        assertThat(booking.getBooker().getName()).isEqualTo("Name");

        verify(userStorage, times(1)).findById(1);
        verify(bookingStorage, times(1)).findById(1);
    }

    @Test
    @DisplayName("Получение всех бронирований")
    void findAll() {
        List<Booking> bookings = List.of(booking);
        when(bookingStorage.findAll()).thenReturn(bookings);
        when(bookingMapper.toBookingDtoList(anyList())).thenAnswer(invocationOnMock -> {
            List<Booking> bookingList = invocationOnMock.getArgument(0);
            return bookingList.stream().map(booking -> {
                BookingDto mockBookingDto = new BookingDto();
                mockBookingDto.setId(booking.getId());
                mockBookingDto.setStart(booking.getStart());
                mockBookingDto.setEnd(booking.getEnd());
                mockBookingDto.setStatus(booking.getStatus());

                UserDto userDto = new UserDto();
                userDto.setId(booking.getBooker().getId());
                userDto.setName(booking.getBooker().getName());
                mockBookingDto.setBooker(userDto);

                ItemDto itemDto = new ItemDto();
                itemDto.setId(booking.getItem().getId());
                itemDto.setName(booking.getItem().getName());
                mockBookingDto.setItem(itemDto);
                return mockBookingDto;
            }).toList();
        });
        List<BookingDto> bookingDtos = bookingService.findAll();
        assertThat(bookingDtos).isNotEmpty();
        assertThat(bookingDtos).hasSize(1);
        assertThat(bookingDtos.getFirst().getId()).isEqualTo(booking.getId());
        assertThat(bookingDtos.getFirst().getBooker().getName()).isEqualTo(booking.getBooker().getName());
        verify(bookingStorage, times(1)).findAll();
        verify(bookingMapper, times(1)).toBookingDtoList(anyList());
    }

    @Test
    @DisplayName("Получение бронирований по ИД владельца")
    void getByOwnerId() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(bookingStorage.findAllByItem_Owner_IdOrderByStartDesc(1)).thenReturn(List.of(booking));
        when(bookingMapper.toBookingDtoList(anyList())).thenAnswer(invocationOnMock -> {
            List<Booking> bookingList = invocationOnMock.getArgument(0);
            return bookingList.stream().map(booking -> {
                BookingDto mockBookingDto = new BookingDto();
                mockBookingDto.setId(booking.getId());
                mockBookingDto.setStart(booking.getStart());
                mockBookingDto.setEnd(booking.getEnd());
                mockBookingDto.setStatus(booking.getStatus());

                UserDto userDto = new UserDto();
                userDto.setId(booking.getBooker().getId());
                userDto.setName(booking.getBooker().getName());
                mockBookingDto.setBooker(userDto);

                ItemDto itemDto = new ItemDto();
                itemDto.setId(booking.getItem().getId());
                itemDto.setName(booking.getItem().getName());
                mockBookingDto.setItem(itemDto);
                return mockBookingDto;
            }).toList();
        });
        List<BookingDto> ownerBookings = bookingService.getByOwnerId(1, BookingState.ALL);
        assertThat(ownerBookings).isNotEmpty();
        assertThat(ownerBookings).hasSize(1);
        assertThat(ownerBookings.getFirst().getId()).isEqualTo(booking.getId());
        assertThat(ownerBookings.getFirst().getBooker().getName()).isEqualTo(booking.getBooker().getName());

        verify(userStorage, times(1)).findById(1);
        verify(bookingStorage, times(1)).findAllByItem_Owner_IdOrderByStartDesc(1);
        verify(bookingMapper, times(1)).toBookingDtoList(anyList());
    }

    @Test
    @DisplayName("Обработка ошибки при получении бронирований несуществующего владельца")
    void getByOwnerIdWithInvalidOwner() {
        int invalidOwnerId = 999;
        when(userStorage.findById(invalidOwnerId)).thenReturn(Optional.empty());
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> bookingService.getByOwnerId(invalidOwnerId, BookingState.ALL))
                .withMessageContaining("Пользователь не найден");
    }
}
