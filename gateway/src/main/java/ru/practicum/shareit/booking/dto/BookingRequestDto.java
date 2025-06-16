package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDto {
    @NotNull(message = "itemId must be provided")
    private Long itemId;

    @NotNull(message = "start must be provided")
    private LocalDateTime start;

    @NotNull(message = "end must be provided")
    private LocalDateTime end;
}