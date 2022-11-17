package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dbMpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class MpaService {
    private final dbMpaStorage dbMpaStorage;

    public MpaService(@Qualifier("mpaDbStorage") dbMpaStorage dbMpaStorage) {
        this.dbMpaStorage = dbMpaStorage;
    }

    public List<Mpa> getMpa() {
        return dbMpaStorage.getMpa();
    }

    public Mpa getMpaById(long mpaId) {
        return dbMpaStorage.getMpaById(mpaId);
    }

    public Mpa createMpa(ResultSet rs) throws SQLException {
        Mpa mpa = getMpaById(rs.getLong("ID"));
        return Mpa.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }
}
