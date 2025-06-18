package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserClient client;
    private final ObjectMapper mapper;

    public UserServiceImpl(UserClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        ResponseEntity<Object> resp = client.createUser(userDto);
        return extractObject(resp, UserDto.class);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        ResponseEntity<Object> resp = client.updateUser(userId, userDto);
        return extractObject(resp, UserDto.class);
    }

    @Override
    public UserDto getUserById(Long userId) {
        ResponseEntity<Object> resp = client.getUser(userId);
        return extractObject(resp, UserDto.class);
    }

    @Override
    public List<UserDto> getAllUsers() {
        ResponseEntity<Object> resp = client.getAllUsers();
        return extractList(resp, new TypeReference<List<UserDto>>() {
        });
    }

    @Override
    public void deleteUser(Long userId) {
        ResponseEntity<Object> resp = client.deleteUser(userId);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(resp.getStatusCode(), "Error deleting user");
        }
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