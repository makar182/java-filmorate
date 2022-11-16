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
import ru.yandex.practicum.filmorate.service.GenreService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreService genreService;
    private final Map<String, Object> parameters;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreService genreService) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreService = genreService;
        this.parameters = new HashMap<>();
    }

    @Override
    public Film addFilm(Film film) {
        List<Genre> genres = new ArrayList<>();
        SimpleJdbcInsert simpleJdbcInsertFilm = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("FILM")
                .usingGeneratedKeyColumns("ID");

        parameters.clear();
        parameters.put("NAME", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDuration());
        parameters.put("RATE", film.getRate());
        parameters.put("MPA_ID", film.getMpa().getId());
        long newFilmId = simpleJdbcInsertFilm.executeAndReturnKey(parameters).longValue();

        if(film.getGenres() != null) {
            batchFilmGenreInsert(newFilmId, new ArrayList<>(film.getGenres()));
            for (Genre genre : film.getGenres()) {
                genres.add(genreService.getGenreById(genre.getId()));
            }
        }

        film.setId(newFilmId);
        film.setMpa(new Mpa(film.getMpa().getId(), getMpaNameById(film.getMpa().getId())));
        film.setGenres(genres);
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

        if(film.getGenres() != null) {
            List<Genre> genres = new ArrayList<>();
            for (Genre genre : film.getGenres()) {
                if(!genres.contains(genre)) {
                    genres.add(genreService.getGenreById(genre.getId()));
                }
            }
            genres.sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
            batchFilmGenreInsert(film.getId(), genres);
            film.setGenres(genres);
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
        String sql = "SELECT FILM.ID, FILM.NAME, FILM.DESCRIPTION, " +
                    "FILM.RELEASE_DATE, FILM.DURATION, FILM.RATE, FILM.MPA_ID, SP_MPA.NAME AS MPA_NAME " +
                    "FROM FILM " +
                    "JOIN SP_MPA on FILM.MPA_ID = SP_MPA.ID";
        return jdbcTemplate.query(sql, this::makeFilm);
    }

    @Override
    public Film getFilmById(Long filmId) {
        String sql = "SELECT FILM.ID, FILM.NAME, FILM.DESCRIPTION, " +
                    "FILM.RELEASE_DATE, FILM.DURATION, FILM.RATE, FILM.MPA_ID, SP_MPA.NAME AS MPA_NAME " +
                    "FROM FILM JOIN SP_MPA " +
                    "ON FILM.MPA_ID = SP_MPA.ID " +
                    "WHERE FILM.ID = ?";
        List<Film> films = jdbcTemplate.query(sql,this::makeFilm, filmId);
        if(films.isEmpty()) {
            return null;
        } else {
            return films.get(0);
        }
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

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        String sql = "SELECT GENRE_ID AS ID FROM FILM_GENRE WHERE FILM_ID = " + rs.getInt("ID");
        List<Genre> genres = jdbcTemplate.query(sql, (rsGenre, rowNumGenre) -> genreService.makeGenre(rsGenre));
        return Film.builder()
                .id(rs.getInt("ID"))
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .rate(rs.getInt("RATE"))
                .mpa(new Mpa(rs.getLong("MPA_ID"), rs.getString("MPA_NAME")))
                .genres(genres)
                .build();
    }

    private String getMpaNameById(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT NAME FROM SP_MPA WHERE ID = " + id, String.class);
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
