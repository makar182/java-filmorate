package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository("dbFilmStorage")
public class dbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public dbFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsertFilm = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("FILM")
                .usingGeneratedKeyColumns("ID");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("NAME", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDuration());
        parameters.put("RATE", film.getRate());
        parameters.put("MPA_ID", film.getMpa().getId());
        long newFilmId = simpleJdbcInsertFilm.executeAndReturnKey(parameters).longValue();


        film.setId(newFilmId);
        //film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql;

        sql = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?" +
                ", RATE = ?, MPA_ID = ? WHERE ID = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getRate(), film.getMpa().getId(), film.getId());

        sql = "DELETE FROM FILM_GENRE WHERE FILM_ID = ?";
        Object[] args = new Object[]{film.getId()};
        jdbcTemplate.update(sql, args);

        return film;
    }

    @Override
    public Film deleteFilm(Film film) {
        String sql = "DELETE FROM FILM WHERE ID = ?";
        Object[] args = new Object[]{film.getId()};
        if (jdbcTemplate.update(sql, args) == 1) {
            return film;
        } else {
            log.info(String.format(String.format("Фильм с ID = %d не найден, поэтому его не удалось удалить!", film.getId())));
            return null;
        }
    }

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT FILM.ID, FILM.NAME, FILM.DESCRIPTION, " +
                "FILM.RELEASE_DATE, FILM.DURATION, FILM.RATE, FILM.MPA_ID, SP_MPA.NAME AS MPA_NAME " +
                "FROM FILM " +
                "JOIN SP_MPA on FILM.MPA_ID = SP_MPA.ID";
        return jdbcTemplate.query(sql, this::makeFilm);
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        String sql = "SELECT FILM.ID, FILM.NAME, FILM.DESCRIPTION, " +
                "FILM.RELEASE_DATE, FILM.DURATION, FILM.RATE, FILM.MPA_ID, SP_MPA.NAME AS MPA_NAME " +
                "FROM FILM JOIN SP_MPA " +
                "ON FILM.MPA_ID = SP_MPA.ID " +
                "WHERE FILM.ID = ?";
        List<Film> films = jdbcTemplate.query(sql, this::makeFilm, filmId);
        if (films.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(films.get(0));
        }
    }

    @Override
    public void addLike(Long userId, Long filmId) {
        String sql;
        sql = "SELECT COUNT(*) FROM FILM_USER_LIKES WHERE USER_ID = ? AND FILM_ID = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, userId, filmId);
        if (count == 0) {
            SimpleJdbcInsert simpleJdbcInsertFilmUserLikes = new SimpleJdbcInsert(this.jdbcTemplate)
                    .withTableName("FILM_USER_LIKES");

            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("USER_ID", userId);
            parameters.put("FILM_ID", filmId);
            simpleJdbcInsertFilmUserLikes.execute(parameters);

            sql = "UPDATE FILM SET RATE = RATE + 1 WHERE ID = ?";
            Object[] args = new Object[]{filmId};
            jdbcTemplate.update(sql, args);
        }
    }

    @Override
    public void deleteLike(Long userId, Long filmId) {
        String sql;
        sql = "SELECT COUNT(*) FROM FILM_USER_LIKES WHERE USER_ID = ? AND FILM_ID = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, userId, filmId);
        if (count == 0) {
            log.info(String.format(String.format("Пользователь с ID = %d не лайкал фильм с ID = %d, потому удалить лайк не удалось!", userId, filmId)));
            throw new ObjectNotExistException(String.format("Пользователь с ID = %d не лайкал фильм с ID = %d, потому удалить лайк не удалось!", userId, filmId));
        }
        sql = "DELETE FROM FILM_USER_LIKES WHERE USER_ID = ? AND FILM_ID = ?";
        Object[] args = new Object[]{userId, filmId};
        jdbcTemplate.update(sql, args);
    }

    @Override
    public void batchFilmGenreInsert(Long filmId, List<Genre> genres) {
        String sql = "DELETE FROM FILM_GENRE WHERE FILM_ID = ?";
        Object[] args = new Object[]{filmId};
        jdbcTemplate.update(sql, args);

        Set<Genre> uniqueGenres = new HashSet<>(genres);
        genres.clear();
        genres.addAll(uniqueGenres);

        jdbcTemplate.batchUpdate("INSERT INTO FILM_GENRE VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, filmId);
                        ps.setLong(2, genres.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return genres.size();
                    }
                });
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .rate(rs.getLong("RATE"))
                .mpa(new Mpa(rs.getLong("MPA_ID"), rs.getString("MPA_NAME")))
                .build();
    }
}
