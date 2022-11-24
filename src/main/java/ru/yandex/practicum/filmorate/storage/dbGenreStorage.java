package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

@Repository("dbGenreStorage")
public class dbGenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public dbGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Long, Genre> getGenres() {
        String sql = "SELECT ID, NAME FROM SP_GENRE";
        List<Genre> genres = jdbcTemplate.query(sql, this::makeGenre);
        Map<Long, Genre> result = new HashMap<>();
        for (Genre genre : genres) {
            result.put(genre.getId(), genre);
        }
        return result;
    }

    public Optional<Genre> getGenreById(long genreId) {
        String sql = "SELECT ID, NAME FROM SP_GENRE WHERE ID = " + genreId;
        List<Genre> genres = jdbcTemplate.query(sql, this::makeGenre);
        if (genres.size() > 0) {
            return Optional.ofNullable(genres.get(0));
        } else {
            return Optional.empty();
        }
    }

    public void addGenresToFilms(List<Film> films) {
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        final Map<Long, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, identity()));
        final String sqlQuery = "SELECT SP_GENRE.ID, SP_GENRE.NAME, FILM_GENRE.FILM_ID " +
                "FROM FILM_GENRE " +
                "JOIN SP_GENRE ON FILM_GENRE.GENRE_ID = SP_GENRE.ID " +
                "WHERE FILM_GENRE.FILM_ID IN (" + inSql +")";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            final Film film = filmById.get(rs.getLong("FILM_ID"));
            List<Genre> genres = film.getGenres();
            Genre genre = makeGenre(rs, 0);
            genres.remove(genre);
            genres.add(genre);
        }, films.stream().map(Film::getId).toArray());
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .build();
    }
}
