package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestStorage extends JpaRepository<ItemRequest, Integer> {
    List<ItemRequest> findByRequesterIdOrderByCreatedDesc(Integer requesterId);
}
