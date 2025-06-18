package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemClient client;
    private final ObjectMapper mapper;

    public ItemServiceImpl(ItemClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public ItemResponseDto create(Long userId, ItemRequestDto dto) {
        ResponseEntity<Object> resp = client.create(userId, dto);
        return extractObject(resp, ItemResponseDto.class);
    }

    @Override
    public ItemResponseDto update(Long userId,
                                  Long itemId,
                                  ItemUpdateDto dto) {
        ResponseEntity<Object> resp = client.update(userId, itemId, dto);
        return extractObject(resp, ItemResponseDto.class);
    }

    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        ResponseEntity<Object> resp = client.getById(userId, itemId);
        return extractObject(resp, ItemResponseDto.class);
    }

    @Override
    public List<ItemResponseDto> getAllByOwner(Long userId, int from, int size) {
        ResponseEntity<Object> resp = client.getAll(userId, from, size);
        return extractList(resp, new TypeReference<List<ItemResponseDto>>() {
        });
    }

    @Override
    public List<ItemResponseDto> search(Long userId,
                                        String text,
                                        int from,
                                        int size) {
        ResponseEntity<Object> resp = client.search(userId, text, from, size);
        return extractList(resp, new TypeReference<List<ItemResponseDto>>() {
        });
    }

    @Override
    public CommentDto addComment(Long userId,
                                 Long itemId,
                                 CommentDto comment) {
        ResponseEntity<Object> resp = client.addComment(userId, itemId, comment);
        return extractObject(resp, CommentDto.class);
    }

    private <T> T extractObject(ResponseEntity<Object> resp, Class<T> cls) {
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(resp.getStatusCode(), "Error from server");
        }
        return mapper.convertValue(resp.getBody(), cls);
    }

    private <T> T extractList(ResponseEntity<Object> resp,
                              TypeReference<T> typeRef) {
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(resp.getStatusCode(), "Error from server");
        }
        return mapper.convertValue(resp.getBody(), typeRef);
    }
}