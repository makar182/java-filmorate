package ru.yandex.practicum.filmorate.storage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("mpaDbStorage")
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> getMpa() {
        String sql = "SELECT ID, NAME FROM SP_MPA";
        return jdbcTemplate.query(sql,this::makeMpa);
    }

    public Mpa getMpaById(long mpaId) {
        String sql = "SELECT ID, NAME FROM SP_MPA WHERE ID = " + mpaId;
        List<Mpa> mpa = jdbcTemplate.query(sql,this::makeMpa);
        if(mpa.size() > 0) {
            return mpa.get(0);
        } else {
            throw new ObjectNotExistException(String.format("Рейтинг MPA c ID=%d не существует!",mpaId));
        }
    }

    private Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return Mpa.builder()
                .id(rs.getInt("ID"))
                .name(rs.getString("NAME"))
                .build();
    }
}
