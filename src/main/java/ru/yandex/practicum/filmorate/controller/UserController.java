package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addToFriends(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
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
