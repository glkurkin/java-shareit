package ru.practicum.shareit.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepository {
    public static final Map<Long, User> users = new ConcurrentHashMap<>();
    public static final Map<Long, Item> items = new ConcurrentHashMap<>();
    public static long itemIdCounter = 1;
}