package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface UserStorage {

    Collection<User> getAll();

    User getUserById(int id);

    User create(User user);

    User update(User newUser);

    Map<User, Set<Integer>> addToFriend(int userId, int friendsId);

    void removeFriend(int userId, int friendsId);

    Collection<User> getFriendsUser(int userId);

    Collection<User> getMutualFriends(int userId, int friendsId);

    void validateUserId(int id);
}
