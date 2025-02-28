package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Service
public class UserService {

    public UserService(@Qualifier("userDbStorage")UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private final UserStorage userStorage;

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public Map<User, Set<Integer>> addToFriend(int userId, int friendsId) {
        return userStorage.addToFriend(userId, friendsId);
    }

    public void removeFriend(int userId, int friendsId) {
        userStorage.removeFriend(userId, friendsId);
    }

    public Collection<User> getFriendsUser(int userId) {
        return userStorage.getFriendsUser(userId);
    }

    public Collection<User> getMutualFriends(int userId, int friendsId) {
        return userStorage.getMutualFriends(userId, friendsId);
    }


}
