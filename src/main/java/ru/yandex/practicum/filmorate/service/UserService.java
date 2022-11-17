package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("dbUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        if (!isUserInfoValid(user)) {
            log.info("Валидация при сохранении нового пользователя не пройдена!");
            throw new ValidationException("Валидация при сохранении нового пользователя не пройдена!");
        }
        validateUserName(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (!isUserInfoValid(user)) {
            log.info(String.format("Валидация при обновлении пользователя №%d не пройдена!", user.getId()));
            throw new ValidationException(String.format("Валидация при обновлении пользователя №%d не пройдена!", user.getId()));
        } else if (!getUsers().contains(user)) {
            log.info(String.format("Попытка обновления несуществующего пользователя №%d!", user.getId()));
            throw new ObjectNotExistException(String.format("Попытка обновления несуществующего пользователя №%d!", user.getId()));
        }
        validateUserName(user);
        return userStorage.updateUser(user);
    }

    public User getUserById(Long userId) {
        Optional<User> user = userStorage.getUserById(userId);
        if (user.isEmpty()) {
            log.info(String.format("Пользователь №%d не найден!", userId));
            throw new ObjectNotExistException(String.format("Пользователь №%d не найден!", userId));
        }
        return user.get();
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addToFriends(Long userId, Long friendId) {
        isUserAndFriendExist(userId, friendId);
        userStorage.addToFriends(friendId, userId);
    }

    public void deleteFromFriends(Long userId, Long friendId) {
        isUserAndFriendExist(userId, friendId);
        userStorage.deleteFromFriends(friendId, userId);
    }

    public List<User> getFriendsByUserId(Long userId) {
        List<Long> friends = userStorage.getFriends(userId);
        if (friends.isEmpty()) {
            return List.of();
        } else {
            return friends.stream()
                    .map(userStorage::getUserById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
    }

    public List<User> getFriendsInCommon(Long userId, Long friendId) {
        isUserAndFriendExist(userId, friendId);

        List<Long> userFriends = userStorage.getFriends(userId);
        List<Long> friendFriends = userStorage.getFriends(friendId);

        if (userFriends.isEmpty() || friendFriends.isEmpty()) {
            return List.of();
        }

        return userFriends.stream()
                .filter(friendFriends::contains)
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void validateUserName(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    private boolean isUserInfoValid(User user) {
        return user.getEmail().contains("@") && user.getBirthday().isBefore(LocalDate.now());
    }

    private void isUserAndFriendExist(Long userId, Long friendId) {
        Optional<User> user = userStorage.getUserById(userId);
        Optional<User> friend = userStorage.getUserById(friendId);

        if (user.isEmpty() && friend.isEmpty()) {
            log.info(String.format("Пользователя №%d и №%d не найдено!", userId, friendId));
            throw new ObjectNotExistException(String.format("Пользователя №%d и №%d не найдено!", userId, friendId));
        } else if (user.isEmpty()) {
            log.info(String.format("Пользователя №%d не найдено!", userId));
            throw new ObjectNotExistException(String.format("Пользователя №%d не найдено!", userId));
        } else if (friend.isEmpty()) {
            log.info(String.format("Пользователя №%d не найдено!", friendId));
            throw new ObjectNotExistException(String.format("Пользователя №%d не найдено!", friendId));
        }
    }
}
