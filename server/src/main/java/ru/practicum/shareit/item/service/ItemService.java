package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Integer userId, ItemDto itemDto);

    ItemDto updateItem(Integer userId, Integer itemId, ItemDto itemDto);

    ItemDto getByItemId(Integer userId, Integer itemId);

    List<ItemDto> findAllByOwnerId(Integer userId);

    List<ItemDto> searchByText(String text);

    CommentDto addComment(CommentDto commentDto, Integer userId, Integer itemId);
}
