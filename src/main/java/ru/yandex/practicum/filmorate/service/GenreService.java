package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dbGenreStorage;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GenreService {
    private final dbGenreStorage dbGenreStorage;

    @Autowired
    public GenreService(@Qualifier("dbGenreStorage") dbGenreStorage dbGenreStorage) {
        this.dbGenreStorage = dbGenreStorage;
    }

    public Map<Long, Genre> getGenres() {
        Map<Long, Genre> result = dbGenreStorage.getGenres();
        log.info("Запрос на получение справочника жанров выполнен!");
        return result;
    }

    public Genre getGenreById(long genreId) {
        Genre genre = dbGenreStorage.getGenreById(genreId).orElseThrow(()->{
            log.info(String.format("Жанр c ID=%d не существует!", genreId));
            throw new ObjectNotExistException(String.format("Жанр c ID=%d не существует!", genreId));
        });
        log.info(String.format("Жанр с ID = %d найден!", genreId));
        return genre;
    }

    public void addGenresToFilms(List<Film> films) {
        dbGenreStorage.addGenresToFilms(films);
    }
}
