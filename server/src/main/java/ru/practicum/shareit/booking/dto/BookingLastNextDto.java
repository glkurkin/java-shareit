package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BookingLastNextDto {
    private Integer bookingId;
    private Integer bookerId;
}
