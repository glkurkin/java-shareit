package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.RequestStorage;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final RequestStorage requestStorage;
    private final UserStorage userStorage;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;

    @Transactional
    @Override
    public ItemRequestDto create(ItemRequestCreateDto itemRequestCreateDto, Integer requesterId) {
        User user = userStorage.findById(requesterId).orElseThrow(() -> new NotFoundExcep("Пользователь не найден"));
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestCreateDto, user);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequester(user);
        return itemRequestMapper.toItemRequestDto(requestStorage.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getAllUserRequests(Integer requesterId) {
        return itemRequestMapper.toItemRequestDtoList(requestStorage.findByRequesterIdOrderByCreatedDesc(requesterId));
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {
        return itemRequestMapper.toItemRequestDtoList(requestStorage.findAll());
    }

    @Override
    public ItemRequestWithItemsDto getByRequstId(Integer requestId) {
        ItemRequest itemRequest = requestStorage.findById(requestId).orElseThrow(() -> new NotFoundExcep("Запрос не найден"));
        List<Item> itemsForRequest = itemStorage.findByRequestIdIn(List.of(requestId));
        List<ItemDto> items = itemMapper.toItemDtoList(itemsForRequest);
        return itemRequestMapper.toItemRequestWithItems(itemRequest, items);
    }

}
