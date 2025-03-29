package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(Long userId, BookingDto bookingDto);

    BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved);

    BookingResponseDto getBookingById(Long userId, Long bookingId);

    List<BookingResponseDto> getBookingsForUser(Long userId, String state);

    List<BookingResponseDto> getBookingsForOwner(Long userId, String state);
}