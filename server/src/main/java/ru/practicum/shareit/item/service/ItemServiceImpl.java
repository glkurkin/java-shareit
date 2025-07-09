package ru.practicum.shareit.item.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.dto.BookingLastNextDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestExcep;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mappers.CommentMapper;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentStorage;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.RequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserStorage userStorage;
    private final CommentStorage commentStorage;
    private final CommentMapper commentMapper;
    private final BookingStorage bookingStorage;
    private final RequestStorage requestStorage;

    @Transactional
    @Override
    public ItemDto addNewItem(Integer userId, ItemDto itemDto) {
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundExcep("Пользователь не найден"));
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(user);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = requestStorage.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundExcep("Запрос на вещь не найден"));
            item.setItemRequest(itemRequest);
        }
        return itemMapper.toItemDto(itemStorage.save(item));
    }

    @Transactional
    @Override
    public ItemDto updateItem(Integer userId, Integer itemId, ItemDto itemDto) {
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundExcep("Пользователь не найден"));
        Item item = itemStorage.findById(itemId).orElseThrow(() -> new NotFoundExcep("Предмет не найден"));
        if (itemDto.getName() != null && !itemDto.getName().isEmpty()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isEmpty()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return itemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    public ItemDto getByItemId(Integer userId, Integer itemId) {
        Item item = itemStorage.findById(itemId).orElseThrow(() -> new NotFoundExcep("Предмет не найден"));
        ItemDto itemDto = itemMapper.toItemDto(item);
        if (item.getOwner().getId().equals(userId)) {
            addBookingInfo(itemDto);
        }
        addComments(itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDto> findAllByOwnerId(Integer ownerId) {
        return itemMapper.toItemDtoList(itemStorage.findAllByOwnerId(ownerId));
    }

    @Override
    public List<ItemDto> searchByText(String text) {
        if (text.isBlank()) {
            return Collections.EMPTY_LIST;
        }
        return itemMapper.toItemDtoList(itemStorage.findBySearchText(text)
                .stream()
                .filter(Item::getAvailable)
                .collect(Collectors.toList()));
    }

    @Transactional
    @Override
    public CommentDto addComment(CommentDto commentDto, Integer userId, Integer itemId) {
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundExcep("Пользователь не найден"));
        Item item = itemStorage.findById(itemId).orElseThrow(() -> new NotFoundExcep("Предмет не найден"));
        Comment comment = commentMapper.toComment(commentDto);
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        comment.setText(commentDto.getText());
        if (bookingStorage.findAllApprovedByItemIdAndBookerId(itemId, userId, LocalDateTime.now()).isEmpty()) {
            throw new BadRequestExcep("Комментарии можно оставлять только к тем вещам, на которые было бронирование");
        }
        commentStorage.save(comment);
        return commentMapper.toCommentDto(comment);
    }

    private void addBookingInfo(ItemDto itemDto) {
        List<Booking> bookings = bookingStorage.findAllByItemId(itemDto.getId());

        Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .reduce((b1, b2) -> b1.getStart().isBefore(b2.getStart()) ? b1 : b2)
                .orElse(null);

        Booking lastBooking = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .reduce((b1, b2) -> b1.getEnd().isAfter(b2.getEnd()) ? b1 : b2)
                .orElse(null);

        itemDto.setNextBooking(nextBooking != null ? BookingLastNextDto.builder()
                .bookingId(nextBooking.getId())
                .bookerId(nextBooking.getBooker().getId())
                .build() : null);
        itemDto.setLastBooking(lastBooking != null ? BookingLastNextDto.builder()
                .bookingId(lastBooking.getId())
                .bookerId(lastBooking.getBooker().getId())
                .build() : null);
    }

    private void addComments(ItemDto itemDto) {
        itemDto.setComments(commentStorage.findAllByItemId(itemDto.getId())
                .stream()
                .map(CommentMapper.MAPPER::toCommentDto)
                .toList());
    }

}
