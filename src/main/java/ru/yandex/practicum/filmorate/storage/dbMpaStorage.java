package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository("mpaDbStorage")
public class dbMpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public dbMpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Long, Mpa> getMpa() {
        String sql = "SELECT ID, NAME FROM SP_MPA";
        List<Mpa> mpaS = jdbcTemplate.query(sql, this::makeMpa);
        Map<Long, Mpa> result = new HashMap<>();
        for (Mpa mpa : mpaS) {
            result.put(mpa.getId(), mpa);
        }
        return result;
    }

    public Optional<Mpa> getMpaById(long mpaId) {
        String sql = "SELECT ID, NAME FROM SP_MPA WHERE ID = " + mpaId;
        List<Mpa> mpa = jdbcTemplate.query(sql, this::makeMpa);
        if (mpa.size() > 0) {
            return Optional.ofNullable(mpa.get(0));
        } else {
            return Optional.empty();
        }
    }

    public List<Film> addMpaToFilms(List<Film> films) {
        Map<Long, Mpa> mpaS = getMpa();
        for (Film film : films) {
            film.setMpa(mpaS.get(film.getMpa().getId()));
        }
        
        return films;
    }

    private Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return Mpa.builder()
                .id(rs.getLong("ID"))
                .name(rs.getString("NAME"))
                .build();
    }
}
