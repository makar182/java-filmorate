package ru.yandex.practicum.filmorate.service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
@Service
public class MpaService {
    private final MpaDbStorage mpaDbStorage;

    public MpaService(@Qualifier("mpaDbStorage") MpaDbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    public List<Mpa> getMpa() {
        return mpaDbStorage.getMpa();
    }

    public Mpa getMpaById(long mpaId) {
        return mpaDbStorage.getMpaById(mpaId);
    }

    public Mpa createMpa(ResultSet rs) throws SQLException {
        Mpa mpa = getMpaById(rs.getLong("ID"));
        return Mpa.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }
}
