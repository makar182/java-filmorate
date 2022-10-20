package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long id = 0L;

    @Override
    public User addUser(User user) {
        if (!isUserInfoValid(user)) {
            log.debug("Валидация при сохранении пользователя не пройдена!");
            throw new ValidationException("Информация о пользователе не проходит условия валидации. " + "Пользователь не добавлен!!");
        }
        validateUserName(user);
        user.setId(++id);
        users.put(user.getId(), user);
        log.debug("Добавлен новый пользователь: " + user);

        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!isUserInfoValid(user)) {
            log.debug("Валидация при обновлении пользователя не пройдена!");
            throw new ValidationException("Информация о пользователе не проходит условия валидации. " + "Пользователь не добавлен!");
        } else if (!users.containsKey(user.getId())) {
            log.debug("Попытка обновления несуществующего пользователя!");
            throw new ObjectNotExistException("Попытка обновления несуществующего пользователя!");
        }
        validateUserName(user);
        users.put(user.getId(), user);
        log.debug("Обновлен пользователь: " + user);

        return user;
    }

    @Override
    public User deleteUser(User user) {
        return null;
    }

    @Override
    public User getUserById(Long userId) {
        return users.get(userId);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    private void validateUserName(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    private boolean isUserInfoValid(User user) {
        return user.getEmail().contains("@") && user.getBirthday().isBefore(LocalDate.now());
    }
}
