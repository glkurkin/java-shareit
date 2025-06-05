package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    @NotBlank(message = "name must be provided")
    private String name;

    @NotBlank(message = "description must be provided")
    private String description;

    @NotNull(message = "available must be provided")
    private Boolean available;
}