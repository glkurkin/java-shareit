package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.dto.UserDto;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        validateEmail(userDto.getEmail());
        if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже используется");
        }
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email обязателен");
        }
        if (!email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
        }
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            if (userRepository.existsByEmailIgnoreCase(userDto.getEmail()) &&
                    !user.getEmail().equalsIgnoreCase(userDto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже используется");
            }
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> list = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            list.add(UserMapper.toUserDto(user));
        }
        return list;
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}