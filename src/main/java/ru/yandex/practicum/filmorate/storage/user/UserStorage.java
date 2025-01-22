package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    Collection<User> getAll();

    User getUserById(int id);

    User create(User user);

    User update(User newUser);

    User addToFriend(int userId, int friendsId);

    User removeFriends(int userId, int friendsId);

    Collection<User> getFriendsUser(int userId);

    Collection<User> getMutualFriends(int userId, int friendsId);

    void validateUserId(int id);

    void cloneSearchEmail(User user);

    void checkOrAddUserName(User user);

    Integer validateUpdate(User newUser);
}
