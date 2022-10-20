package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUserById(Long userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new ObjectNotExistException(String.format("Пользователь №%d не найден!", userId));
        }
        return user;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addToFriends(Long userIdFollower, Long userIdFollowed) {
        User follower = userStorage.getUserById(userIdFollower);
        User followed = userStorage.getUserById(userIdFollowed);

        if (follower == null && followed == null) {
            throw new ObjectNotExistException(String.format("Пользователя №%d и №%d не найдено!", userIdFollower, userIdFollowed));
        } else if (follower == null) {
            throw new ObjectNotExistException(String.format("Пользователя №%d не найдено!", userIdFollower));
        } else if (followed == null) {
            throw new ObjectNotExistException(String.format("Пользователя №%d не найдено!", userIdFollowed));
        }

        follower.addFriend(userIdFollowed);
        followed.addFriend(userIdFollower);
    }

    public void deleteFromFriends(Long userIdFollower, Long userIdFollowed) {
        userStorage.getUserById(userIdFollowed).deleteFriend(userIdFollower);
        userStorage.getUserById(userIdFollower).deleteFriend(userIdFollowed);
    }

    public Set<User> getFriendsByUserId(Long userId) {
        Set<Long> friends = userStorage.getUserById(userId).getFriends();
        Set<User> result = new HashSet<>();

        for (Long friend : friends) {
            result.add(getUserById(friend));
        }
        return result;
    }

    public Set<User> getFriendsInCommon(Long userIdFollower, Long userIdFollowed) {
        Set<User> result = new HashSet<>();
        Set<Long> userIdFollowerFriends = userStorage.getUserById(userIdFollower).getFriends();
        Set<Long> userIdFollowedFriends = userStorage.getUserById(userIdFollowed).getFriends();

        for (Long followerFriend : userIdFollowerFriends) {
            if (userIdFollowedFriends.contains(followerFriend)) {
                result.add(userStorage.getUserById(followerFriend));
            }
        }
        return result;
    }
}
