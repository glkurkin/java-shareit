package ru.practicum.shareit.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.repository.InMemoryRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {
    private final ConcurrentHashMap<Long, User> localUsers = new ConcurrentHashMap<>();
    private long userIdCounter = 1;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email обязателен");
        }
        if (!userDto.getEmail().contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
        }
        boolean exists = localUsers.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(userDto.getEmail()));
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже используется");
        }
        User user = new User();
        user.setId(userIdCounter++);
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        localUsers.put(user.getId(), user);
        InMemoryRepository.users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = localUsers.get(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        if (userDto.getEmail() != null) {
            if (!userDto.getEmail().contains("@")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
            }
            boolean exists = localUsers.values().stream()
                    .anyMatch(u -> !u.getId().equals(userId) && u.getEmail().equalsIgnoreCase(userDto.getEmail()));
            if (exists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже используется");
            }
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        InMemoryRepository.users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = localUsers.get(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> list = new ArrayList<>();
        for (User user : localUsers.values()) {
            list.add(UserMapper.toUserDto(user));
        }
        return list;
    }

    @Override
    public void deleteUser(Long userId) {
        localUsers.remove(userId);
        InMemoryRepository.users.remove(userId);
    }
}