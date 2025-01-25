package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> userFriends = new HashMap<>();

    private int id = 1;

    public int getNextId() {
        return id++;
    }

    @Override
    public Collection<User> getAll() {
        log.info("GET, all users");
        return users.values();
    }

    @Override
    public User getUserById(int id) {
        validateUserId(id);
        return users.get(id);
    }


    @Override
    public User create(User user) {
        log.info("POST, create user {}", user);
        cloneSearchEmail(user);
        user.setId(getNextId());
        checkOrAddUserName(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        log.info("PUT, update User {}", newUser);
        Integer id = validateUpdate(newUser);
        User oldUser = users.get(id);
        String newEmail = newUser.getEmail();
        String newLogin = newUser.getLogin();
        oldUser.setName(newUser.getName());

        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }
        if (!oldUser.getEmail().equals(newEmail)) {
            oldUser.setEmail(newEmail);
        }
        if (!oldUser.getLogin().equals(newLogin)) {
            oldUser.setLogin(newLogin);
        }
        return oldUser;
    }

    @Override
    public Map<User, Set<Integer>> addToFriend(int userId, int friendsId) {
        validateUserId(userId);
        validateUserId(friendsId);

        if (checkUserFriends(userId)) {
            userFriends.get(userId).add(friendsId);
        } else {
            Set<Integer> friendsIds = new HashSet<>();
            friendsIds.add(friendsId);
            userFriends.put(userId, friendsIds);
        }

        if (checkUserFriends(friendsId)) {
            userFriends.get(friendsId).add(userId);
        } else {
            Set<Integer> friendsIds = new HashSet<>();
            friendsIds.add(userId);
            userFriends.put(friendsId, friendsIds);
        }
        return Map.of(users.get(userId), userFriends.get(userId),
                users.get(friendsId), userFriends.get(friendsId));
    }

    @Override
    public void removeFriend(int userId, int friendsId) {
        validateUserId(userId);
        validateUserId(friendsId);

        if (checkUserFriends(userId)) {
            Set<Integer> friendsIds1 = userFriends.get(userId);
            friendsIds1.remove(friendsId);
            if (friendsIds1.isEmpty()) {
                userFriends.remove(userId);
            } else {
                userFriends.put(userId, friendsIds1);
            }

            Set<Integer> friendsIds2 = userFriends.get(friendsId);
            friendsIds2.remove(userId);
            if (friendsIds2.isEmpty()) {
                userFriends.remove(friendsId);
            } else {
                userFriends.put(friendsId, friendsIds2);
            }
        }
    }

    @Override
    public Collection<User> getFriendsUser(int userId) {
        validateUserId(userId);
        Set<Integer> friendsIds = userFriends.get(userId);
        List<User> friends = new ArrayList<>();
        if (!checkUserFriends(userId)) {
            return friends;
        }

        for (int id : friendsIds) {
            friends.add(users.get(id));
        }
        return friends;
    }

    @Override
    public Collection<User> getMutualFriends(int userId, int friendsId) {
        validateUserId(userId);
        validateUserId(friendsId);

        List<Integer> mutualId = new ArrayList<>(userFriends.get(userId));
        mutualId.retainAll(userFriends.get(friendsId));

        List<User> mutualFriends = new ArrayList<>();
        for (Integer id : mutualId) {
            mutualFriends.add(users.get(id));
        }
        return mutualFriends;
    }

    @Override
    public void validateUserId(int id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id: " + id + " не найден");
        }
    }

    private boolean checkUserFriends(int id) {
        return userFriends.containsKey(id);
    }


    private void cloneSearchEmail(User user) {
        log.info("cloneSearchEmail start for {}", user);
        String newEmail = user.getEmail();
        for (User userMap : users.values()) {
            if (userMap.getEmail().equals(newEmail)) {
                String msg = "Этот Email уже используется";
                log.error(msg);
                throw new ValidationException(msg);
            }
        }
    }


    private void checkOrAddUserName(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }

    private Integer validateUpdate(User newUser) {
        log.info("validateUpdate start for {}", newUser);
        checkOrAddUserName(newUser);
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
}
