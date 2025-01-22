package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class UserService {

    private InMemoryUserStorage inMemoryUserStorage;

    public Collection<User> getAll() {
        return inMemoryUserStorage.getAll();
    }

    public User create(User user) {
        return inMemoryUserStorage.create(user);
    }

    public User update(User newUser) {
        return inMemoryUserStorage.update(newUser);
    }

    public User addToFriend(int userId, int friendsId) {
        return inMemoryUserStorage.addToFriend(userId, friendsId);
    }

    public User removeFriends(int userId, int friendsId) {
        return inMemoryUserStorage.removeFriends(userId, friendsId);
    }

    public Collection<User> getFriendsUser(int userId) {
        return inMemoryUserStorage.getFriendsUser(userId);
    }

    public Collection<User> getMutualFriends(int userId, int friendsId) {
        return inMemoryUserStorage.getMutualFriends(userId, friendsId);
    }


}
