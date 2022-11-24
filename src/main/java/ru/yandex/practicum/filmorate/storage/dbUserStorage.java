package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository("dbUserStorage")
public class dbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public dbUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public User addUser(User user) {
        SimpleJdbcInsert simpleJdbcInsertUser = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("ID");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("EMAIL", user.getEmail());
        parameters.put("LOGIN", user.getLogin());
        parameters.put("NAME", user.getName());
        parameters.put("BIRTHDAY", user.getBirthday());
        long newUserId = simpleJdbcInsertUser.executeAndReturnKey(parameters).longValue();

        user.setId(newUserId);
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql;

        sql = "UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ?" +
                "WHERE ID = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public User deleteUser(User user) {
        String sql;
        Object[] args;

        sql = "DELETE FROM FRIENDS WHERE USER_ID = ? OR FRIEND_ID = ?";
        args = new Object[]{user.getId(), user.getId()};
        jdbcTemplate.update(sql, args);

        sql = "DELETE FROM USERS WHERE ID = ?";
        args = new Object[]{user.getId()};
        if (jdbcTemplate.update(sql, args) == 1) {
            return user;
        } else {
            log.info(String.format("Не удалось удалить пользователя с ID = %d," +
                    " т.к. такого пользователя не нашлось!", user.getId()));
            return null;
        }
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        String sql = "SELECT ID, EMAIL, LOGIN, NAME, BIRTHDAY " +
                "FROM USERS " +
                "WHERE USERS.ID = ?";

        List<User> users = jdbcTemplate.query(sql, this::makeUser, userId);
        if (users.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(users.get(0));
        }
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM USERS";
        return jdbcTemplate.query(sql, this::makeUser);
    }

    @Override
    public List<Long> getFriends(Long userId) {
        String sql = "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ?";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> rs.getLong("FRIEND_ID")), userId);
    }

    @Override
    public void addToFriends(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, friendId, userId);
        if (count == 0) {
            SimpleJdbcInsert simpleJdbcInsertFriend = new SimpleJdbcInsert(this.jdbcTemplate)
                    .withTableName("FRIENDS");

            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("USER_ID", friendId);
            parameters.put("FRIEND_ID", userId);
            simpleJdbcInsertFriend.execute(parameters);
        }
    }

    @Override
    public void deleteFromFriends(Long userId, Long friendId) {
        String sql = "DELETE FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        Object[] args = new Object[]{friendId, userId};
        jdbcTemplate.update(sql, args);
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("ID"))
                .email(rs.getString("EMAIL"))
                .login(rs.getString("LOGIN"))
                .name(rs.getString("NAME"))
                .birthday(rs.getDate("BIRTHDAY").toLocalDate())
                .build();
    }
}