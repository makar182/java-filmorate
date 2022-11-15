package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("GenreDbStorage")
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getGenres() {
        String sql = "SELECT ID, NAME FROM SP_GENRE";
        return jdbcTemplate.query(sql,this::createGenre);
    }

    public Genre getGenreById(long genreId) {
        String sql = "SELECT ID, NAME FROM SP_GENRE WHERE ID = " + genreId;
        return jdbcTemplate.query(sql,this::createGenre).get(0);
    }

    private Genre createGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("ID"))
                .name(rs.getString("NAME"))
                .build();
    }
}
