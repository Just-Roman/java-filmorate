package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingDbStorage;

import java.time.LocalDate;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreDbStorage.class, MpaRatingDbStorage.class, UserDbStorage.class})
class UserDbStorageTest {

    private final UserDbStorage userDbStorage;

    User user1 = User.builder()
            .email("yandex@mail.ru")
            .login("turbo")
            .birthday(LocalDate.of(2024, 12, 12))
            .build();

    User user2 = User.builder()
            .email("Arrange@mail.ru")
            .login("Away")
            .birthday(LocalDate.of(2024, 11, 11))
            .build();

    User user3 = User.builder()
            .email("bobr@mail.ru")
            .login("bobr")
            .birthday(LocalDate.of(2024, 11, 11))
            .build();

    @Test
    void getAll() {
//        Arrange
        User createdUser1 = userDbStorage.create(user1);
        User createdUser2 = userDbStorage.create(user2);

//        Act
        List<User> allUsers = (List<User>) userDbStorage.getAll();

//        Assert
        Assertions.assertThat(allUsers).isEqualTo(List.of(createdUser1, createdUser2));
    }

    @Test
    void getUserById() {
//        Arrange
        User createdUser = userDbStorage.create(user1);

//        Act
        User getUser = userDbStorage.getUserById(createdUser.getId());

//        Assert
        Assertions.assertThat(createdUser).isEqualTo(getUser);
    }

    @Test
    void create() {
//        Act
        User createdUser = userDbStorage.create(user1);

//        Assert
        Assertions.assertThat(createdUser).isEqualTo(user1);

    }

    @Test
    void update() {
//        Arrange
        User createdUser = userDbStorage.create(user1);
        createdUser.setLogin("Titan");
        createdUser.setEmail("titan@mail.ru");

//        Act
        userDbStorage.update(createdUser);
        User getUser = userDbStorage.getUserById(createdUser.getId());

//        Assert
        Assertions.assertThat(getUser.getLogin()).isEqualTo("Titan");
        Assertions.assertThat(getUser.getEmail()).isEqualTo("titan@mail.ru");
    }

    @Test
    void addToFriend() {
//        Arrange
        Integer createdUserId1 = userDbStorage.create(user1).getId();
        User createdUser2 = userDbStorage.create(user2);
        userDbStorage.addToFriend(createdUserId1, createdUser2.getId());

//        Act
        List<User> firends = (List<User>) userDbStorage.getFriendsUser(createdUserId1);

//        Assert
        Assertions.assertThat(firends.size()).isEqualTo(1);
        Assertions.assertThat(firends.getFirst()).isEqualTo(createdUser2);
    }

    @Test
    void removeFriend() {
//        Arrange
        Integer createdUserId1 = userDbStorage.create(user1).getId();
        User createdUser2 = userDbStorage.create(user2);
        userDbStorage.addToFriend(createdUserId1, createdUser2.getId());
        List<User> firends = (List<User>) userDbStorage.getFriendsUser(createdUserId1);
        Assertions.assertThat(firends.size()).isEqualTo(1);
        Assertions.assertThat(firends.getFirst()).isEqualTo(createdUser2);

//        Act
        userDbStorage.removeFriend(createdUserId1, createdUser2.getId());
        List<User> firendsUpdate = (List<User>) userDbStorage.getFriendsUser(createdUserId1);

//        Assert
        Assertions.assertThat(firendsUpdate.size()).isEqualTo(0);
    }

    @Test
    void getFriendsUser() {
        //        Arrange
        Integer createdUserId1 = userDbStorage.create(user1).getId();
        User createdUser2 = userDbStorage.create(user2);
        userDbStorage.addToFriend(createdUserId1, createdUser2.getId());

//        Act
        List<User> firends = (List<User>) userDbStorage.getFriendsUser(createdUserId1);

//        Assert
        Assertions.assertThat(firends.size()).isEqualTo(1);
        Assertions.assertThat(firends.getFirst()).isEqualTo(createdUser2);
    }

    @Test
    void getMutualFriends() {
//        Arrange
        Integer userId1 = userDbStorage.create(user1).getId();
        Integer userId2 = userDbStorage.create(user2).getId();
        Integer userId3 = userDbStorage.create(user3).getId();

        userDbStorage.addToFriend(userId1, userId2);
        userDbStorage.addToFriend(userId1, userId3);
        userDbStorage.addToFriend(userId2, userId3);

//        Act
        List<User> mutualFriends = (List<User>) userDbStorage.getMutualFriends(userId1, userId2);

//        Assert
        Assertions.assertThat(mutualFriends.size()).isEqualTo(1);
        Assertions.assertThat(mutualFriends.getFirst()).isEqualTo(user3);

    }


}