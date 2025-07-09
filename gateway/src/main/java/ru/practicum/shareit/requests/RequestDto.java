package ru.practicum.shareit.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestDto {
    @NotBlank
    private String description;
}
