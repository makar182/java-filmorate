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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage) {
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
        User user = userStorage.getUserById(userId);
        if (user == null) {
            log.info(String.format("Пользователь №%d не найден!", userId));
            throw new ObjectNotExistException(String.format("Пользователь №%d не найден!", userId));
        }
        return user;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addToFriends(Long userIdFollower, Long userIdFollowed) {
        isFollowerAndFollowedExist(userIdFollower, userIdFollowed);

        User follower = userStorage.getUserById(userIdFollower);
        User followed = userStorage.getUserById(userIdFollowed);

        follower.addFriend(userIdFollowed);
        followed.addFriend(userIdFollower);
    }

    public void deleteFromFriends(Long userIdFollower, Long userIdFollowed) {
        userStorage.getUserById(userIdFollowed).deleteFriend(userIdFollower);
        userStorage.getUserById(userIdFollower).deleteFriend(userIdFollowed);
    }

    public List<User> getFriendsByUserId(Long userId) {
        Set<Long> friends = userStorage.getUserById(userId).getFriends();

        return friends.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getFriendsInCommon(Long userIdFollower, Long userIdFollowed) {
        isFollowerAndFollowedExist(userIdFollower, userIdFollowed);

        User follower = userStorage.getUserById(userIdFollower);
        User followed = userStorage.getUserById(userIdFollowed);

        Set<Long> userIdFollowerFriends = follower.getFriends();
        Set<Long> userIdFollowedFriends = followed.getFriends();

        return userIdFollowerFriends.stream()
                .filter(userIdFollowedFriends::contains)
                .map(userStorage::getUserById)
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

    private void isFollowerAndFollowedExist(Long userIdFollower, Long userIdFollowed) {
        User follower = userStorage.getUserById(userIdFollower);
        User followed = userStorage.getUserById(userIdFollowed);

        if (follower == null && followed == null) {
            log.info(String.format("Пользователя №%d и №%d не найдено!", userIdFollower, userIdFollowed));
            throw new ObjectNotExistException(String.format("Пользователя №%d и №%d не найдено!", userIdFollower, userIdFollowed));
        } else if (follower == null) {
            log.info(String.format("Пользователя №%d не найдено!", userIdFollower));
            throw new ObjectNotExistException(String.format("Пользователя №%d не найдено!", userIdFollower));
        } else if (followed == null) {
            log.info(String.format("Пользователя №%d не найдено!", userIdFollowed));
            throw new ObjectNotExistException(String.format("Пользователя №%d не найдено!", userIdFollowed));
        }
    }
}
