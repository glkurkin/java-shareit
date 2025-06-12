package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingRequestDto dto);

    BookingDto approveBooking(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getAllOwnByBooker(Long bookerId, String state, int from, int size);

    List<BookingDto> getAllOwnByOwner(Long ownerId, String state, int from, int size);
}