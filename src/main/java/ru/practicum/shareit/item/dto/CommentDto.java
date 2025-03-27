package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class CommentDto {
    private final Long id;
    private final String text;
    private final String authorName;
    private final LocalDateTime created;
}