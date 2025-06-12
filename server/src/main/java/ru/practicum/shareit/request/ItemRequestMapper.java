package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ResponseItemDto;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequestResponseDto toResponseDto(
            ru.practicum.shareit.request.ItemRequest request,
            List<Item> items) {
        List<ResponseItemDto> itemDtos = items.stream()
                .map(i -> new ResponseItemDto(
                        i.getId(),
                        i.getName(),
                        i.getOwnerId()))
                .collect(Collectors.toList());

        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }
}