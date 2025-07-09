package ru.practicum.shareit.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.users.dto.UserCreateDto;
import ru.practicum.shareit.users.dto.UserUpdateDto;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable Integer id) {
        log.info("Выводим пользователя с ИД = {} ", id);
        return userClient.findById(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Выводим всех пользователей");
        return userClient.getAllUsers();
    }

    @PostMapping
    public ResponseEntity<Object> addUser(@Valid @RequestBody UserCreateDto uCreateDto) {
        log.info("Добавляем пользователя {}", uCreateDto);
        return userClient.addUser(uCreateDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updUser(@Valid @RequestBody UserUpdateDto uUpdateDto, @PathVariable Integer id) {
        log.info("Обновляем пользователя с ИД = {}, данные для обновления - {}", id, uUpdateDto);
        return userClient.updUser(uUpdateDto, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Integer id) {
        log.info("Удаляем пользователя с ИД = {}", id);
        return userClient.deleteUser(id);
    }
}
