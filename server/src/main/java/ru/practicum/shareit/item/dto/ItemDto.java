package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingLastNextDto;
import ru.practicum.shareit.user.UserDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private UserDto owner;
    private List<CommentDto> comments;
    private BookingLastNextDto lastBooking;
    private BookingLastNextDto nextBooking;
    private Integer requestId;
}
