package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("dbUserStorage")
public class dbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Object> parameters;

    @Autowired
    public dbUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.parameters = new HashMap<>();
    }


    @Override
    public User addUser(User user) {
        SimpleJdbcInsert simpleJdbcInsertUser = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("ID");

        parameters.clear();
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

//        sql = "DELETE FROM FILM_GENRE WHERE FILM_ID = ?";
//        Object[] args = new Object[]{film.getId()};
//        jdbcTemplate.update(sql, args);

//        if(film.getGenres() != null) {
//            List<Genre> genres = new ArrayList<>();
//            for (Genre genre : film.getGenres()) {
//                if(!genres.contains(genre)) {
//                    genres.add(genreService.getGenreById(genre.getId()));
//                }
//            }
//            genres.sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
//            batchFilmGenreInsert(film.getId(), genres);
//            film.setGenres(genres);
//        }
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

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("ID"))
                .email(rs.getString("EMAIL"))
                .login(rs.getString("LOGIN"))
                .name(rs.getString("NAME"))
                .birthday(rs.getDate("BIRTHDAY").toLocalDate())
                .build();
    }

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

            parameters.clear();
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
}