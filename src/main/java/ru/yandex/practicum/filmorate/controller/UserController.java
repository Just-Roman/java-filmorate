package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Collection<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{userId}/friends")
    public Collection<User> getFriendsUser(@PathVariable int userId) {
        return userService.getFriendsUser(userId);
    }

    @GetMapping("/{userId}/friends/common/{friendsId}")
    public Collection<User> getMutualFriends(@PathVariable int userId, @PathVariable int friendsId) {
        return userService.getMutualFriends(userId, friendsId);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return userService.update(newUser);
    }

    @PutMapping("/{userId}/friends/{friendsId}")
    public Map<User, Set<Integer>> addToFriend(@PathVariable int userId, @PathVariable int friendsId) {
        return userService.addToFriend(userId, friendsId);
    }

    @DeleteMapping("/{userId}/friends/{friendsId}")
    public void removeFriend(@PathVariable int userId, @PathVariable int friendsId) {
        userService.removeFriend(userId, friendsId);
    }


}
