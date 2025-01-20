package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

     Collection<User> getAll();

     User create(User user);

     User update(User newUser);

     void cloneSearchEmail(User user);

     void checkOrAddUserName(User user);

     Integer validateUpdate(User newUser);
}
