package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestExcep;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final BookingMapper bookingMapper;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Transactional
    @Override
    public BookingDto addNewBooking(Integer userId, BookingRequestDto bookingRequestDto) {
        Item item = itemStorage.findById(bookingRequestDto.getItemId()).orElseThrow(() -> new NotFoundExcep("Предмет не найден"));
        Booking booking = bookingMapper.requestToBooking(bookingRequestDto);
        booking.setBooker(userStorage.findById(userId).orElseThrow(() -> new NotFoundExcep("Пользователь не найден")));
        booking.setItem(itemStorage.findById(bookingRequestDto.getItemId()).orElseThrow(() -> new NotFoundExcep("Предмет не найден")));
        if (item.getAvailable()) {
            booking.setStatus(BookingStatus.WAITING);
        } else throw new BadRequestExcep("Вещь недоступна для брони");
        return bookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    @Transactional
    @Override
    public BookingDto updateBooking(Integer userId, Integer bookingId, boolean approved) {
        User user = userStorage.findById(userId).orElseThrow(() -> new BadRequestExcep("Пользователь не найден"));
        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() -> new NotFoundExcep("Бронирование не найдено"));
        if (!booking.getItem().getOwner().getId().equals(user.getId())) {
            throw new BadRequestExcep("Бронирование подтверждает только владелец предмета");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else booking.setStatus(BookingStatus.CANCELED);
        bookingStorage.save(booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getByUserId(Integer userId, Integer bookingId) {
        User user = userStorage.findById(userId).orElseThrow(() -> new BadRequestExcep("Пользователь не найден"));
        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() -> new NotFoundExcep("Бронирование не найдено"));
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findAll() {
        return bookingMapper.toBookingDtoList(bookingStorage.findAll());
    }


    @Override
    public List<BookingDto> getByOwnerId(Integer ownerId, BookingState bookingState) {
        userStorage.findById(ownerId).orElseThrow(() -> new NotFoundExcep("Пользователь не найден"));
        List<Booking> bookings = List.of();
        LocalDateTime now = LocalDateTime.now();
        switch (bookingState) {
            case ALL -> bookings = bookingStorage.findAllByItem_Owner_IdOrderByStartDesc(ownerId);
            case CURRENT ->
                    bookings = bookingStorage.findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now);
            case PAST -> bookings = bookingStorage.findByItem_Owner_IdAndEndBeforeOrderByStartDesc(ownerId, now);
            case FUTURE -> bookings = bookingStorage.findByItem_Owner_IdAndStartAfterOrderByStartDesc(ownerId, now);
            case WAITING ->
                    bookings = bookingStorage.findAllByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case REJECTED ->
                    bookings = bookingStorage.findAllByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
        }
        return bookingMapper.toBookingDtoList(bookings);
    }

}

