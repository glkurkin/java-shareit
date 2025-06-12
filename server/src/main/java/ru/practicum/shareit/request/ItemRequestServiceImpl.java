package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ResponseItemDto;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository reqRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;

    @Override
    public ItemRequestResponseDto createRequest(Long userId, CreateItemRequestDto dto) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        var req = new ItemRequest(null, dto.getDescription(), LocalDateTime.now(), user);
        var saved = reqRepo.save(req);
        return toResponseDto(saved);
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        if (!userRepo.existsById(userId))
            throw new NotFoundException("Пользователь не найден");
        return reqRepo.findByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllOtherRequests(Long userId, int from, int size) {
        if (!userRepo.existsById(userId))
            throw new NotFoundException("Пользователь не найден");
        var page = PageRequest.of(from / size, size);
        return reqRepo.findByRequestorIdNotOrderByCreatedDesc(userId, page).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest req = reqRepo.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<Item> items = itemRepo.findByRequestIdOrderById((requestId));
        return ItemRequestMapper.toResponseDto(req, items);
    }

    private ItemRequestResponseDto toResponseDto(ItemRequest req) {
        var items = itemRepo.findAll().stream()
                .filter(i -> i.getRequest() != null && i.getRequest().getId().equals(req.getId()))
                .map(i -> new ResponseItemDto(i.getId(), i.getName(), i.getOwnerId()))
                .collect(Collectors.toList());

        return ItemRequestResponseDto.builder()
                .id(req.getId())
                .description(req.getDescription())
                .created(req.getCreated())
                .items(items)
                .build();
    }
}
