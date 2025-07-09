package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final String USERID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader(USERID_HEADER) Integer requesterId, @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        log.info("Создаем запрос на вещь с описанием {}", itemRequestCreateDto.getDescription());
        return itemRequestService.create(itemRequestCreateDto, requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests() {
        log.info("Выводим все запросы");
        return itemRequestService.getAllRequests();
    }

    @GetMapping
    public List<ItemRequestDto> getAllUserRequests(@RequestHeader(USERID_HEADER) Integer requesterId) {
        log.info("Выводим все запросы от пользователя с ИД = {}", requesterId);
        return itemRequestService.getAllUserRequests(requesterId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getItemRequestById(@PathVariable Integer requestId) {
        log.info("Ищем вещи по запросу с ИД = {}", requestId);
        return itemRequestService.getByRequstId(requestId);
    }
}
