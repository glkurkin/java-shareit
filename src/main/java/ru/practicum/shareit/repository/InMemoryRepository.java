package ru.practicum.shareit.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import java.util.HashMap;
import java.util.Map;

public class InMemoryRepository {
    public static final Map<Long, User> users = new HashMap<>();
    public static final Map<Long, Item> items = new HashMap<>();
    public static long itemIdCounter = 1;
}
