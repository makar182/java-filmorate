package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.interfaces.Marker;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.*;

@Validated
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Validated({Marker.OnCreate.class})
    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @Validated({Marker.OnUpdate.class})
    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addToFriends(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        userService.addToFriends(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFromFriends(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        userService.deleteFromFriends(userId, friendId);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendsByUserId(@PathVariable("id") Long userId) {
        return userService.getFriendsByUserId(userId);
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/{id}/friends/common/{otherUserId}")
    public List<User> getCommonFriendsByUserId(@PathVariable("id") Long userId, @PathVariable("otherUserId") Long otherUserId) {
        return userService.getFriendsInCommon(userId, otherUserId);
    }
}
