package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Object> parameters;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.parameters = new HashMap<>();
    }

    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsertFilm = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("FILM")
                .usingGeneratedKeyColumns("ID");

        parameters.clear();
        parameters.put("NAME", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDuration());
        parameters.put("RATE", film.getRate());
        parameters.put("MPA", film.getMpa());
        long newFilmId = simpleJdbcInsertFilm.executeAndReturnKey(parameters).longValue();

        if(film.getGenres() != null) {
            batchFilmGenreInsert(newFilmId, new ArrayList<>(film.getGenres()));
        }

        return Film.builder()
                .id(newFilmId)
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .rate(film.getRate())
                .mpa(film.getMpa())
                .genres(film.getGenres())
                .build();
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

        if(film.getGenres() != null) {
            batchFilmGenreInsert(film.getId(), new ArrayList<>(film.getGenres()));
        }

        return film;
    }

    @Override
    public Film deleteFilm(Film film) {
        String sql = "DELETE FROM FILM WHERE ID = ?";
        Object[] args = new Object[]{film.getId()};
        if(jdbcTemplate.update(sql, args) == 1) {
            return film;
        } else {
            return null;
        }
    }

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATE, MPA_ID FROM FILM";
        return jdbcTemplate.query(sql, this::createFilm);
    }

    @Override
    public Film getFilmById(Long filmId) {
        String sql = "SELECT ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATE, MPA_ID FROM FILM WHERE ID = " + filmId;
        return jdbcTemplate.queryForObject(sql,this::createFilm);
    }

    @Override
    public void addLike(Long userId, Long filmId) {
        SimpleJdbcInsert simpleJdbcInsertFilmUserLikes = new SimpleJdbcInsert(this.jdbcTemplate)
                                                                             .withTableName("FILM_USER_LIKES");
        parameters.clear();
        parameters.put("USER_ID", userId);
        parameters.put("FILM_ID", filmId);
        simpleJdbcInsertFilmUserLikes.execute(parameters);
    }

    @Override
    public void deleteLike(Long userId, Long filmId) {
        String sql = "DELETE FROM FILM_USER_LIKES WHERE USER_ID = ? AND FILM_ID = ?";
        Object[] args = new Object[]{userId, filmId};
        jdbcTemplate.update(sql, args);
    }

    private Film createFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getInt("ID"))
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .rate(rs.getInt("RATE"))
                .mpa(rs.getObject("MPA_ID", Mpa.class))
                .build();
    }

    private void batchFilmGenreInsert(Long filmId, List<Genre> genres) {
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
}
