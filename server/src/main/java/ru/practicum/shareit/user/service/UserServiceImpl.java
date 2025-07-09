package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedEmailExcep;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Transactional
    @Override
    public User addUser(User user) {
        if (emailCheck(user)) throw new DuplicatedEmailExcep("Данная почта уже используется");
        userStorage.save(user);
        return user;
    }

    @Transactional
    @Override
    public User updUser(Integer id, User updUser) {
        if (updUser.getEmail() != null && emailCheck(updUser))
            throw new DuplicatedEmailExcep("Данная почта уже используется");
        User updatedUser = userStorage.findById(id).orElseThrow(() -> new NotFoundExcep("Пользователь с ID = " + id + " не найден"));
        if (updUser.getName() != null) {
            updatedUser.setName(updUser.getName());
        }
        if (updUser.getEmail() != null) {
            updatedUser.setEmail(updUser.getEmail());
        }
        return userStorage.save(updatedUser);
    }

    @Override
    public User getUserById(Integer id) {
        return userStorage.findById(id).orElseThrow(() -> new NotFoundExcep("Пользователь с ID = " + id + " не найден"));
    }

    @Override
    public void delete(Integer id) {
        userStorage.deleteById(id);
    }

    @Override
    public List<User> getAll() {
        return userStorage.findAll();
    }

    private boolean emailCheck(User user) {
        return getAll().stream().anyMatch(i -> user.getEmail().equals(i.getEmail()));
    }
}
