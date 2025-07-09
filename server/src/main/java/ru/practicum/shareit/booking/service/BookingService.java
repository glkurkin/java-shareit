package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

public interface BookingService {
    BookingDto addNewBooking(Integer userId, BookingRequestDto bookingRequestDto);

    BookingDto updateBooking(Integer userId, Integer bookingId, boolean approved);

    BookingDto getByUserId(Integer userId, Integer bookingId);

    List<BookingDto> findAll();

    List<BookingDto> getByOwnerId(Integer ownerId, BookingState bookingState);
}
