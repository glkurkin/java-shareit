package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestCreateDto itemRequestCreateDto, Integer requesterId);

    List<ItemRequestDto> getAllUserRequests(Integer requesterId);

    List<ItemRequestDto> getAllRequests();

    ItemRequestWithItemsDto getByRequstId(Integer requesterId);
}
