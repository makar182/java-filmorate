package ru.yandex.practicum.filmorate.exceptions;

public class ObjectNotExistException extends RuntimeException{
    public ObjectNotExistException(String message) {
        super(message);
    }
}
