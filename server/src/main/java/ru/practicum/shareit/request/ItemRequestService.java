package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto createRequest(Long userId, @Valid CreateItemRequestDto dto);

    List<ItemRequestResponseDto> getOwnRequests(Long userId);

    List<ItemRequestResponseDto> getAllOtherRequests(Long userId, int from, int size);

    ItemRequestResponseDto getRequestById(Long userId, Long requestId);
}