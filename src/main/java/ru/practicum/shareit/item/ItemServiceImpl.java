package ru.practicum.shareit.item;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.repository.InMemoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Sharer-User-Id отсутствует");
        }
        if (!InMemoryRepository.users.containsKey(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поле 'available' обязательно");
        }

        Item item = new Item();
        item.setId(InMemoryRepository.itemIdCounter++);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(InMemoryRepository.users.get(userId));

        InMemoryRepository.items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не передан X-Sharer-User-Id");
        }
        Item item = InMemoryRepository.items.get(itemId);
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена");
        }
        if (!item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только владелец может редактировать вещь");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = InMemoryRepository.items.get(itemId);
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        return InMemoryRepository.items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();
        return InMemoryRepository.items.values().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                                item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}