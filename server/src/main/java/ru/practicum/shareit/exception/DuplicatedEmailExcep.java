package ru.practicum.shareit.exception;

public class DuplicatedEmailExcep extends RuntimeException {
    public DuplicatedEmailExcep(String message) {
        super(message);
    }
}
