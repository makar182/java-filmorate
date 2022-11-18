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
        User newUser = userStorage.addUser(user);
        log.info(String.format("Пользователь %s добавлен в хранилище!", newUser));
        return newUser;
    }

    public User updateUser(User user) {
        if (!isUserInfoValid(user)) {
            log.info(String.format("Валидация при обновлении пользователя с ID = %d не пройдена!", user.getId()));
            throw new ValidationException(String.format("Валидация при обновлении пользователя с ID = %d не пройдена!", user.getId()));
        } else if (!getUsers().contains(user)) {
            log.info(String.format("Попытка обновления несуществующего пользователя с ID = %d!", user.getId()));
            throw new ObjectNotExistException(String.format("Попытка обновления несуществующего пользователя с ID = %d!", user.getId()));
        }
        validateUserName(user);
        User newUser = userStorage.updateUser(user);
        log.info(String.format("Пользователь %s обновлен!", newUser));
        return newUser;
    }

    public User getUserById(Long userId) {
        User user = userStorage.getUserById(userId).orElseThrow(() -> {
            log.info(String.format("Пользователь с ID = %d не найден!", userId));
            throw new ObjectNotExistException(String.format("Пользователь с ID = %d не найден!", userId));
        });

        log.info(String.format("Пользователь с ID = %d найден!", userId));
        return user;
    }

    public List<User> getUsers() {
        List<User> result = userStorage.getUsers();
        log.info("Запрос на получение списка пользователей getUsers выполнен!");
        return result;
    }

    public void addToFriends(Long userId, Long friendId) {
        isUserAndFriendExist(userId, friendId);
        userStorage.addToFriends(friendId, userId);
        log.info(String.format("Пользователь с ID = %d добавлен в друзья пользователю с ID = %d!", userId, friendId));
    }

    public void deleteFromFriends(Long userId, Long friendId) {
        isUserAndFriendExist(userId, friendId);
        userStorage.deleteFromFriends(friendId, userId);
        log.info(String.format("Пользователь с ID = %d удалён из друзей пользователя с ID = %d!", userId, friendId));
    }

    public List<User> getFriendsByUserId(Long userId) {
        List<Long> friends = userStorage.getFriends(userId);
        if (friends.isEmpty()) {
            log.info(String.format("У пользователя с ID = %d друзья не найдены!", userId));
            return List.of();
        } else {
            List<User> result = friends.stream()
                    .map(userStorage::getUserById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            log.info(String.format("У пользователя с ID = %d друзья найдены!", userId));
            return result;
        }
    }

    public List<User> getFriendsInCommon(Long userId, Long friendId) {
        isUserAndFriendExist(userId, friendId);

        List<Long> userFriends = userStorage.getFriends(userId);
        List<Long> friendFriends = userStorage.getFriends(friendId);

        if (userFriends.isEmpty() || friendFriends.isEmpty()) {
            log.info(String.format("У пользователей с ID = %d и %d нет общих друзей, " +
                    "т.к. у одного из польлзователей список друзей пустой!", userId, friendId));
            return List.of();
        }

        List<User> result = userFriends.stream()
                .filter(friendFriends::contains)
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        log.info(String.format("Найдены общие друзья у пользователей с ID = %d и %d!", userId, friendId));
        return result;
    }

    private void validateUserName(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
            log.info(String.format(String.format("Пользователю с ID = %d выполнена подстановка в поле Name значение поля Login!", user.getId())));
        }
    }

    private boolean isUserInfoValid(User user) {
        return user.getEmail().contains("@") && user.getBirthday().isBefore(LocalDate.now());
    }

    private void isUserAndFriendExist(Long userId, Long friendId) {
        Optional<User> user = userStorage.getUserById(userId);
        Optional<User> friend = userStorage.getUserById(friendId);

        if (user.isEmpty() && friend.isEmpty()) {
            log.info(String.format("Пользователя с ID = %d и с ID = %d не найдено!", userId, friendId));
            throw new ObjectNotExistException(String.format("Пользователя с ID = %d и с ID = %d не найдено!", userId, friendId));
        } else if (user.isEmpty()) {
            log.info(String.format("Пользователя с ID = %d не найдено!", userId));
            throw new ObjectNotExistException(String.format("Пользователя с ID = %d не найдено!", userId));
        } else if (friend.isEmpty()) {
            log.info(String.format("Пользователя с ID = %d не найдено!", friendId));
            throw new ObjectNotExistException(String.format("Пользователя с ID = %d не найдено!", friendId));
        }
    }
}
