package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    User deleteUser(User user);

    Optional<User> getUserById(Long userId);

    List<User> getUsers();

    List<Long> getFriends(Long userId);

    void addToFriends(Long userId, Long friendId);

    void deleteFromFriends(Long userId, Long friendId);
}
