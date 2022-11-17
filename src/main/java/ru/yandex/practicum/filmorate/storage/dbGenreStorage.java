package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("GenreDbStorage")
public class dbGenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public dbGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getGenres() {
        String sql = "SELECT ID, NAME FROM SP_GENRE";
        return jdbcTemplate.query(sql, this::makeGenre);
    }

    public Genre getGenreById(long genreId) {
        String sql = "SELECT ID, NAME FROM SP_GENRE WHERE ID = " + genreId;
        List<Genre> genres = jdbcTemplate.query(sql, this::makeGenre);
        if (genres.size() > 0) {
            return genres.get(0);
        } else {
            throw new ObjectNotExistException(String.format("Жанра c ID=%d не существует!", genreId));
        }
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("ID"))
                .name(rs.getString("NAME"))
                .build();
    }
}