package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserDbStorage implements UserStorage {
    protected final JdbcTemplate jdbc;

    public UserDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String GET_EMAIL = "SELECT email FROM users WHERE email = ?;";
    private static final String GET_ID = "SELECT id FROM users WHERE id = ?;";
    private static final String INSERT_USERS = "INSERT INTO users (email, login, name, " +
            "birthday) VALUES (?, ?, ?, ?)";
    private static final String INSERT_FRIENDSHIP = """
            INSERT INTO friendship (first_user_id, second_user_id, status)
            VALUES (?, ?, ?)
            """;

    private static final String UPDATE_STATUS_FRIENDSHIP = """
             UPDATE friendship
             SET  status ?
             WHERE first_user_id = ?, second_user_id = ?
            """;

    private static final String GET_FRIENDSHIP_BY_IDS = """
            SELECT *
            FROM friendship
            WHERE first_user_id = ? AND second_user_id = ?;
            """;

    private static final String GET_FRIENDS_BY_ID = """
            SELECT STRING_AGG(second_user_id, ',') AS second_user_id
            FROM friendship
            WHERE first_user_id = ?  AND status = true;
            """;

    private static final String GET_MUTUAL_FRIENDS = """
            SELECT second_user_id AS friend_id
            FROM friendship
            WHERE first_user_id = ?
              AND status = true
                    
            INTERSECT
                    
            SELECT second_user_id AS friend_id
            FROM friendship
            WHERE first_user_id = ?
              AND status = true;
            """;

    private static final String GET_BY_ID = "SELECT * FROM users WHERE id = ?;";
    private static final String GET_ALL = "SELECT * FROM users;";

    private static final String UPDATE_USERS = """
             UPDATE users
             SET email = ?, login = ?, name = ?, birthday = ?
             WHERE id = ?
            """;
    private static final String DELETE_FRIENDSHIP = """
            DELETE FROM friendship
            WHERE first_user_id = ? AND second_user_id = ?
            """;

    @Override
    public Collection<User> getAll() {
        return jdbc.query(GET_ALL, UserDbStorage::getUserMapper);
    }

    @Override
    public User getUserById(int id) {
        return jdbc.queryForObject(GET_BY_ID, UserDbStorage::getUserMapper, id);
    }

    @Override
    public User create(User user) {
        cloneSearchEmail(user.getEmail());
        checkOrAddUserName(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_USERS, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        Integer id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(id);

        if ((user.getFriends() != null)) {
            Set<Integer> ids = user.getFriends();
            for (Integer idFriend : ids) {
                addToFriend(id, idFriend);
            }
        }

        return jdbc.queryForObject(GET_BY_ID, UserDbStorage::getUserMapper, id);
    }

    @Override
    public User update(User updateUser) {
        Integer id = updateUser.getId();
        validateUserId(id);

        jdbc.update(UPDATE_USERS,
                updateUser.getEmail(),
                updateUser.getLogin(),
                updateUser.getName(),
                updateUser.getBirthday(),
                id
        );

        return jdbc.queryForObject(GET_BY_ID, UserDbStorage::getUserMapper, id);
    }

    @Override
    public Map<User, Set<Integer>> addToFriend(int userId, int friendsId) {
        validateUserId(userId);
        validateUserId(friendsId);
        List<Friendship> friendships = jdbc.query(GET_FRIENDSHIP_BY_IDS,
                UserDbStorage::getFriendshipMapper, userId, friendsId);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (friendships.isEmpty()) {
            jdbc.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(INSERT_FRIENDSHIP);
                stmt.setInt(1, userId);
                stmt.setInt(2, friendsId);
                stmt.setBoolean(3, true);
                return stmt;
            }, keyHolder);

            jdbc.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(INSERT_FRIENDSHIP);
                stmt.setInt(1, friendsId);
                stmt.setInt(2, userId);
                stmt.setBoolean(3, false);
                return stmt;
            }, keyHolder);
        } else {
            Friendship friendship = friendships.getFirst();
            if (!friendship.getStatus()) {
                jdbc.update(UPDATE_STATUS_FRIENDSHIP,
                        true,
                        friendship.getFirstUserId(),
                        friendship.getSecondUserId()
                );
            }
        }

        String friends = jdbc.queryForObject(GET_FRIENDS_BY_ID, String.class, userId);
        Set<Integer> friendIds = Arrays.stream(friends.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        User user = jdbc.queryForObject(GET_BY_ID, UserDbStorage::getUserMapper, userId);
        return Map.of(user, friendIds);

    }

    @Override
    public void removeFriend(int userId, int friendsId) {
        validateUserId(userId);
        validateUserId(friendsId);

        jdbc.update(DELETE_FRIENDSHIP, userId, friendsId);
    }

    @Override
    public Collection<User> getFriendsUser(int userId) {
        validateUserId(userId);
        String friends = jdbc.queryForObject(GET_FRIENDS_BY_ID, String.class, userId);
        List<User> users = new ArrayList<>();

        if (friends != null) {
            String[] ids = friends.split(",\\s*");
            for (String id : ids) {
                User user = jdbc.queryForObject(GET_BY_ID, UserDbStorage::getUserMapper, Integer.parseInt(id));
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public Collection<User> getMutualFriends(int userId, int friendsId) {
        validateUserId(userId);
        validateUserId(friendsId);

        List<Integer> friends = jdbc.queryForList(GET_MUTUAL_FRIENDS, Integer.class, userId, friendsId);
        List<User> users = new ArrayList<>();
        if (!friends.isEmpty()) {
            for (Integer id : friends) {
                User user = jdbc.queryForObject(GET_BY_ID, UserDbStorage::getUserMapper, id);
                users.add(user);
            }
        }
        return users;
    }

    private static User getUserMapper(ResultSet resultSet, int rowNum) throws SQLException {
        Timestamp birthday = resultSet.getTimestamp("birthday");

        return User.builder()
                .id(resultSet.getInt("id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(birthday.toLocalDateTime().toLocalDate())
                .build();
    }

    private static Friendship getFriendshipMapper(ResultSet resultSet, int rowNum) throws SQLException {

        return Friendship.builder()
                .firstUserId(resultSet.getInt("first_user_id"))
                .secondUserId(resultSet.getInt("second_user_id"))
                .status(resultSet.getBoolean("status"))
                .build();
    }

    private void cloneSearchEmail(String newEmail) {
        List<String> emails = jdbc.queryForList(GET_EMAIL, String.class, newEmail);
        if (!emails.isEmpty()) {
            throw new ValidationException("Этот Email уже используется");
        }
    }

    private void validateUserId(int id) {
        List<Integer> ids = jdbc.queryForList(GET_ID, Integer.class, id);

        if (ids.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    private void checkOrAddUserName(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }


}
