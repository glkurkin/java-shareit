package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemResponseDto create(Long userId, ItemRequestDto dto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        if (dto.getName().isBlank()) {
            throw new BadRequestException("name must be provided");
        }
        if (dto.getDescription().isBlank()) {
            throw new BadRequestException("description must be provided");
        }

        Item item = new Item();
        item.setOwnerId(userId);
        item.setName(dto.getName().trim());
        item.setDescription(dto.getDescription().trim());
        item.setAvailable(dto.getAvailable());
        Item saved = itemRepository.save(item);

        return ItemResponseDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .available(saved.getAvailable())
                .lastBooking(null)
                .nextBooking(null)
                .comments(Collections.emptyList())
                .build();
    }

    @Override
    public ItemResponseDto update(Long userId, Long itemId, ItemUpdateDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!item.getOwnerId().equals(userId)) {
            throw new NotFoundException("Вещь с id=" + itemId + " не найдена у пользователя " + userId);
        }

        if (dto.getName() != null && dto.getName().isBlank()) {
            throw new BadRequestException("name must be provided");
        }
        if (dto.getDescription() != null && dto.getDescription().isBlank()) {
            throw new BadRequestException("description must be provided");
        }

        if (dto.getName() != null) {
            item.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription().trim());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        Item updated = itemRepository.save(item);

        return ItemResponseDto.builder()
                .id(updated.getId())
                .name(updated.getName())
                .description(updated.getDescription())
                .available(updated.getAvailable())
                .lastBooking(null)
                .nextBooking(null)
                .comments(Collections.emptyList())
                .build();
    }

    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        BookingDto lastBooking = null;
        BookingDto nextBooking = null;

        List<Comment> commentsRaw = commentRepository.findByItemIdInOrderByCreatedDesc(Collections.singletonList(itemId));
        List<CommentDto> comments = commentsRaw.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();
    }

    @Override
    public List<ItemResponseDto> getAllByOwner(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByOwnerIdOrderById(userId, pageRequest);

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Comment> allComments = commentRepository.findByItemIdInOrderByCreatedDesc(itemIds);

        Map<Long, List<CommentDto>> commentsByItemId = new HashMap<>();
        for (Comment c : allComments) {
            commentsByItemId
                    .computeIfAbsent(c.getItem().getId(), __ -> new ArrayList<>())
                    .add(CommentMapper.toCommentDto(c));
        }

        return items.stream().map(item -> {
            BookingDto lastBooking = null;
            BookingDto nextBooking = null;

            List<CommentDto> commentsForThis = commentsByItemId.getOrDefault(item.getId(), Collections.emptyList());

            return ItemResponseDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .lastBooking(lastBooking)
                    .nextBooking(nextBooking)
                    .comments(commentsForThis)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemResponseDto> search(Long userId, String text, int from, int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.searchByText(text, PageRequest.of(from / size, size));
        return items.stream()
                .map(item -> ItemResponseDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .lastBooking(null)
                        .nextBooking(null)
                        .comments(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        boolean hasPastBooking = bookingRepository.findByBookerIdOrderByStartDesc(userId, PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .anyMatch(b -> b.getItem().getId().equals(itemId)
                        && b.getStatus() == BookingStatus.APPROVED
                        && b.getEnd().isBefore(LocalDateTime.now()));

        if (!hasPastBooking) {
            throw new BadRequestException("Нельзя оставить комментарий: не было завершённого бронирования этой вещи");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment.setAuthor(author);

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saved);
    }
}