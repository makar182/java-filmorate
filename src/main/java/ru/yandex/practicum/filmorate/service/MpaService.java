package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dbMpaStorage;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MpaService {
    private final dbMpaStorage dbMpaStorage;

    @Autowired
    public MpaService(@Qualifier("mpaDbStorage") dbMpaStorage dbMpaStorage) {
        this.dbMpaStorage = dbMpaStorage;
    }

    public Map<Long, Mpa> getMpa() {
        Map<Long, Mpa> result = dbMpaStorage.getMpa();
        log.info("Запрос на получение справочника MPA выполнен!");
        return result;
    }

    public Mpa getMpaById(long mpaId) {
        Mpa mpa = dbMpaStorage.getMpaById(mpaId).orElseThrow(()->{
            log.info(String.format("Рейтинг MPA c ID=%d не существует!", mpaId));
            throw new ObjectNotExistException(String.format("Рейтинг MPA c ID=%d не существует!", mpaId));
        });
        log.info(String.format("MPA с ID = %d найден!", mpaId));
        return mpa;
    }

    public List<Film> addMpaToFilms(List<Film> films) {
        return dbMpaStorage.addMpaToFilms(films);
    }
}
