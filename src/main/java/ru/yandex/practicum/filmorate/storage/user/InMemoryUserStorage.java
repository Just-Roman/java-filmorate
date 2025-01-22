package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.IdGenerator;

import java.util.*;

@RequiredArgsConstructor
@Component
public class InMemoryUserStorage implements UserStorage {

    private final IdGenerator idGenerator;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public Collection<User> getAll() {
//        log.debug("GET, all users");
        return users.values();
    }

    @Override
    public User getUserById(int id) {
        validateUserId(id);
        return users.get(id);
    }


    @Override
    public User create(User user) {
//        log.debug("POST, create user {}", user);
        cloneSearchEmail(user);
        user.setId(idGenerator.getNextId());
        checkOrAddUserName(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
//        log.debug("PUT, update User {}", newUser);
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

    @Override
    public User addToFriend(int userId, int friendsId) {
        validateUserId(userId);
        validateUserId(friendsId);

        User user = users.get(userId);
        User userFriends = users.get(friendsId);

        user.getFriends().add(friendsId);
        userFriends.getFriends().add(userId);

        users.put(userId, user);
        users.put(friendsId, userFriends);

        return userFriends;
    }

    @Override
    public User removeFriends(int userId, int friendsId) {
        validateUserId(userId);
        validateUserId(friendsId);

        User user = users.get(userId);
        User userFriends = users.get(friendsId);

        user.getFriends().remove(friendsId);
        userFriends.getFriends().remove(userId);

        users.put(userId, user);
        users.put(friendsId, userFriends);

        return userFriends;
    }

    @Override
    public Collection<User> getFriendsUser(int userId) {
        validateUserId(userId);
        User user = users.get(userId);
        Set<Integer> friendsId = user.getFriends();
        List<User> userFriends = new ArrayList<>();

        for (int id : friendsId) {
            userFriends.add(users.get(id));
        }
        return userFriends;
    }

    @Override
    public Collection<User> getMutualFriends(int userId, int friendsId) {
        validateUserId(userId);
        validateUserId(friendsId);

        User user1 = users.get(userId);
        User user2 = users.get(friendsId);

        Set<Integer> user1FriendsId = user1.getFriends();
        Set<Integer> user2FriendsId = user2.getFriends();
        List<User> mutualFriends = new ArrayList<>();

        for (int id1 : user1FriendsId) {
            for (int id2 : user2FriendsId) {
                if (id1 == id2) mutualFriends.add(users.get(id1));
            }
        }
        return mutualFriends;
    }

    @Override
    public void validateUserId(int id) {
        if (!users.containsKey(id)) throw new NotFoundException("Пользователь с id=%d не найден");
    }

    @Override
    public void cloneSearchEmail(User user) {
//        log.debug("cloneSearchEmail start for {}", user);
        String newEmail = user.getEmail();
        for (User userMap : users.values()) {
            if (userMap.getEmail().equals(newEmail)) {
                String msg = "Этот Email уже используется";
//                log.error(msg);
                throw new ValidationException(msg);
            }
        }
    }

    @Override
    public void checkOrAddUserName(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }

    @Override
    public Integer validateUpdate(User newUser) {
//        log.debug("validateUpdate start for {}", newUser);
        checkOrAddUserName(newUser);
        String newEmail = newUser.getEmail();
        if (newUser.getId() == null) {
            String msg = "Id должен быть указан";
//            log.error(msg);
            throw new ValidationException(msg);
        }
        Integer id = newUser.getId();
        if (!users.containsKey(id)) {
            String msg = "Пользователь с id = " + newUser.getId() + " не найден";
//            log.error(msg);
            throw new NotFoundException(msg);
        }
        if (!newEmail.equals(users.get(id).getEmail())) {
            cloneSearchEmail(newUser);
        }
        return id;
    }
}
