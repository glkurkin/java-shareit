package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        BookingItemDto itemDto = new BookingItemDto(
                booking.getItem().getId(),
                booking.getItem().getName()
        );
        BookingBookerDto bookerDto = new BookingBookerDto(
                booking.getBooker().getId()
        );

        return BookingDto.builder()
                .id(booking.getId())
                .item(itemDto)
                .booker(bookerDto)
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }
}