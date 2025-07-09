package ru.practicum.shareit.items.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateDto {
    @NotBlank(message = "Коммент не должен быть пустым")
    private String text;
}
