package ru.practicum.shareit.items.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ItemCreateDto {
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @NotNull(message = "Описание не может быть null")
    private String description;
    @NotNull(message = "Available не может быть null")
    private Boolean available;
    private Integer requestId;
}
