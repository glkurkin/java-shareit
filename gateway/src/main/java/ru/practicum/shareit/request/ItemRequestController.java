package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestResponseDto createRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody CreateItemRequestDto dto
    ) {
        return service.createRequest(userId, dto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getOwn(
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        return service.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllOther(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getAllOtherRequests(userId, from, size);
    }

    @GetMapping("/{id}")
    public ItemRequestResponseDto getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long id
    ) {
        return service.getRequestById(userId, id);
    }
}