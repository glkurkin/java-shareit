package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentStorage extends JpaRepository<Comment, Integer> {
    List<Comment> findAllByItemId(Integer id);
}
