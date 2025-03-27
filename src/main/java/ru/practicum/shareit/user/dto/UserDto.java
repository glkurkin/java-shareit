package ru.practicum.shareit.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserDto {
    private final Long id;
    private final String name;
    private final String email;
}