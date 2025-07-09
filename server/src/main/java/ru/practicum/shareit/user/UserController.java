package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody User user) {
        log.info("Пользователь {} добавлен", user.getName());
        return UserMapper.MAPPER.toUserDto(userService.addUser(user));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto update(@RequestBody User user, @PathVariable Integer id) {
        log.info("Данные пользователя с ID = {} обновлены", id);
        return UserMapper.MAPPER.toUserDto(userService.updUser(id, user));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAll() {
        log.info("Выводим всех пользователей");
        List<UserDto> users = UserMapper.MAPPER.toUserDtoList(userService.getAll());
        return users;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getById(@PathVariable Integer id) {
        log.info("Данные пользователя с ID = {} выведены", id);
        return UserMapper.MAPPER.toUserDto(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        log.info("Удаляем пользователя с ID = {} ", id);
        userService.delete(id);
    }

}
