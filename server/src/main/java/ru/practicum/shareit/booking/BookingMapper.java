package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = BookingMapper.class)
public interface BookingMapper {

    BookingDto toBookingDto(Booking booking);

    Booking requestToBooking(BookingRequestDto bookingRequestDto);

    default List<BookingDto> toBookingDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toBookingDto)
                .collect(Collectors.toList());
    }
}