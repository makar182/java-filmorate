package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;


@Slf4j
@Repository("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long id = 0L;

    @Override
    public User addUser(User user) {
        user.setId(++id);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User deleteUser(User user) {
        return null;
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public List<Long> getFriends(Long userId) {
        return users.get(userId).getFriends();
    }

    @Override
    public void addToFriends(Long userId, Long friendId) {
        users.get(userId).getFriends().add(friendId);
    }

    @Override
    public void deleteFromFriends(Long userId, Long friendId) {
        users.get(userId).getFriends().remove(friendId);
    }
}
