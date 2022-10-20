package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;
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
        user.setId(++id);
        users.put(user.getId(), user);
        log.debug("Добавлен новый пользователь: " + user);
        return user;
    }

    @Override
    public User updateUser(User user) {
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
}
