package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = ItemRequestMapper.class)
public interface ItemRequestMapper {
    @Mapping(target = "requesterId", source = "requester.id")
    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "id", ignore = true)
    ItemRequest toItemRequest(ItemRequestCreateDto itemRequestDto, User requester);

    ItemRequestWithItemsDto toItemRequestWithItems(ItemRequest itemRequest, List<ItemDto> items);

    default List<ItemRequestDto> toItemRequestDtoList(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(this::toItemRequestDto)
                .collect(Collectors.toList());
    }
}
