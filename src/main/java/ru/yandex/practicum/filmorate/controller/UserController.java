package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAll() {
        log.debug("GET, all users");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.debug("POST, create user {}", user);
        cloneSearchEmail(user);
        user.setId(getNextId());
        checkUserName(user);
        users.put(user.getId(), user);
        return user;
    }


    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.debug("PUT, update User {}", newUser);
        Integer id = validateUpdate(newUser);
        User oldUser = users.get(id);
        String newEmail = newUser.getEmail();
        String newLogin = newUser.getLogin();
        oldUser.setName(newUser.getName());
        if (newUser.getBirthday() != null) oldUser.setBirthday(newUser.getBirthday());
        if (!oldUser.getEmail().equals(newEmail)) oldUser.setEmail(newEmail);
        if (!oldUser.getLogin().equals(newLogin)) oldUser.setLogin(newLogin);
        return oldUser;
    }

    private void cloneSearchEmail(User user) {
        log.debug("cloneSearchEmail start for {}", user);
        String newEmail = user.getEmail();
        for (User userMap : users.values()) {
            if (userMap.getEmail().equals(newEmail)) {
                String msg = "Этот Email уже используется";
                log.error(msg);
                throw new ValidationException(msg);
            }
        }
    }

    private void checkUserName(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }

    private Integer validateUpdate(User newUser) {
        log.debug("validateUpdate start for {}", newUser);
        checkUserName(newUser);
        String newEmail = newUser.getEmail();
        if (newUser.getId() == null) {
            String msg = "Id должен быть указан";
            log.error(msg);
            throw new ValidationException(msg);
        }
        Integer id = newUser.getId();
        if (!users.containsKey(id)) {
            String msg = "Пользователь с id = " + newUser.getId() + " не найден";
            log.error(msg);
            throw new NotFoundException(msg);
        }
        if (!newEmail.equals(users.get(id).getEmail())) {
            cloneSearchEmail(newUser);
        }
        return id;
    }


    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
