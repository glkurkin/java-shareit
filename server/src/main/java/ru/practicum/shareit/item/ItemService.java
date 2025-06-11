package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {

    ItemResponseDto create(Long userId, ItemRequestDto dto);

    ItemResponseDto update(Long userId, Long itemId, ItemUpdateDto dto);

    ItemResponseDto getById(Long userId, Long itemId);

    List<ItemResponseDto> getAllByOwner(Long userId, int from, int size);

    List<ItemResponseDto> search(Long userId, String text, int from, int size);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}