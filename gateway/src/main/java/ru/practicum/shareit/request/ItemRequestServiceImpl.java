package ru.practicum.shareit.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final RequestClient client;
    private final ObjectMapper mapper;

    public ItemRequestServiceImpl(RequestClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public ItemRequestResponseDto createRequest(Long userId, CreateItemRequestDto dto) {
        ResponseEntity<Object> resp = client.createRequest(userId, dto);
        return extractObject(resp, ItemRequestResponseDto.class);
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        ResponseEntity<Object> resp = client.getOwnRequests(userId);
        return extractList(resp, new TypeReference<List<ItemRequestResponseDto>>() {
        });
    }

    @Override
    public List<ItemRequestResponseDto> getAllOtherRequests(Long userId,
                                                            int from,
                                                            int size) {
        ResponseEntity<Object> resp = client.getAllOtherRequests(userId, from, size);
        return extractList(resp, new TypeReference<List<ItemRequestResponseDto>>() {
        });
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        ResponseEntity<Object> resp = client.getRequestById(userId, requestId);
        return extractObject(resp, ItemRequestResponseDto.class);
    }

    private <T> T extractObject(ResponseEntity<Object> resp, Class<T> cls) {
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(resp.getStatusCode(), "Error from server");
        }
        return mapper.convertValue(resp.getBody(), cls);
    }

    private <T> T extractList(ResponseEntity<Object> resp, TypeReference<T> typeRef) {
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(resp.getStatusCode(), "Error from server");
        }
        return mapper.convertValue(resp.getBody(), typeRef);
    }
}