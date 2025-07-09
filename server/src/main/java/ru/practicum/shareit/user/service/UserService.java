package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User addUser(User user);

    User updUser(Integer id, User updUser);

    User getUserById(Integer id);

    void delete(Integer id);

    List<User> getAll();
}
