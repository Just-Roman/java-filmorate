package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserDbStorage implements UserStorage{


    @Override
    public Collection<User> getAll() {
        return List.of();
    }

    @Override
    public User getUserById(int id) {
        return null;
    }

    @Override
    public User create(User user) {
        return null;
    }

    @Override
    public User update(User newUser) {
        return null;
    }

    @Override
    public Map<User, Set<Integer>> addToFriend(int userId, int friendsId) {
        return Map.of();
    }

    @Override
    public void removeFriend(int userId, int friendsId) {

    }

    @Override
    public Collection<User> getFriendsUser(int userId) {
        return List.of();
    }

    @Override
    public Collection<User> getMutualFriends(int userId, int friendsId) {
        return List.of();
    }

    @Override
    public void validateUserId(int id) {

    }
}
