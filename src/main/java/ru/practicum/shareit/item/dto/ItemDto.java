package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ItemDto {
    private final Long id;
    private final String name;
    private final String description;
    private final Boolean available;
}