package ru.practicum.shareit.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryRepository {
    private static final Map<Long, User> USERS_INTERNAL = new HashMap<>();
    private static final Map<Long, Item> ITEMS_INTERNAL = new HashMap<>();

    public static long itemIdCounter = 1;

    public static Map<Long, User> getUsers() {
        return Collections.unmodifiableMap(USERS_INTERNAL);
    }

    public static void putUser(User user) {
        USERS_INTERNAL.put(user.getId(), user);
    }

    public static User getUser(Long id) {
        return USERS_INTERNAL.get(id);
    }

    public static void removeUser(Long id) {
        USERS_INTERNAL.remove(id);
    }

    public static Map<Long, Item> getItems() {
        return Collections.unmodifiableMap(ITEMS_INTERNAL);
    }

    public static void putItem(Item item) {
        ITEMS_INTERNAL.put(item.getId(), item);
    }

    public static Item getItem(Long id) {
        return ITEMS_INTERNAL.get(id);
    }

    public static void removeItem(Long id) {
        ITEMS_INTERNAL.remove(id);
    }
}