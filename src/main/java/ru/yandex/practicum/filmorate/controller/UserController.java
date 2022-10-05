package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Set<User> users = new HashSet<>();
    private int id = 0;

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        if (!isUserInfoValid(user)) {
            log.debug("Валидация при сохранении пользователя не пройдена!");
            throw new ValidationException("Информация о пользователе не проходит условия валидации. " +
                    "Пользователь не добавлен!!");
        }
        validateUserName(user);
        user.setId(++id);
        users.add(user);
        log.debug("Добавлен новый пользователь: " + user);

        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (!isUserInfoValid(user)) {
            log.debug("Валидация при обновлении пользователя не пройдена!");
            throw new ValidationException("Информация о пользователе не проходит условия валидации. " +
                    "Пользователь не добавлен!");
        } else if (!users.contains(user)) {
            log.debug("Попытка обновления несуществующего пользователя!");
            throw new ValidationException("Попытка обновления несуществующего пользователя!");
        }
        validateUserName(user);
        users.remove(user);
        users.add(user);
        log.debug("Обновлен пользователь: " + user);

        return user;
    }

    @GetMapping
    public Set<User> getUsers() {
        return users;
    }

    private boolean isUserInfoValid(User user) {
        return user.getEmail().contains("@")
                && user.getBirthday().isBefore(LocalDate.now());
    }

    private void validateUserName(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
    }
}
