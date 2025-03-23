package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private Long userIdCounter = 1L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null || !EMAIL_PATTERN.matcher(userDto.getEmail()).matches()) {
            throw new IllegalArgumentException("Некорректный email");
        }
        if (users.values().stream().anyMatch(user -> user.getEmail().equals(userDto.getEmail()))) {
            throw new IllegalArgumentException("Email уже используется");
        }

        User user = new User();
        user.setId(userIdCounter++);
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        if (userDto.getEmail() != null) {
            if (!EMAIL_PATTERN.matcher(userDto.getEmail()).matches()) {
                throw new IllegalArgumentException("Некорректный email");
            }
            if (users.values().stream().anyMatch(u -> !u.getId().equals(userId) && u.getEmail().equals(userDto.getEmail()))) {
                throw new IllegalArgumentException("Email уже используется");
            }
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }
}