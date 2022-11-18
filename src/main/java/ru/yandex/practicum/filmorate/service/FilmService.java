package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreService genreService;
    private final MpaService mpaService;

    @Autowired
    public FilmService(@Qualifier("dbFilmStorage") FilmStorage filmStorage, GenreService genreService, MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    public Film addFilm(Film film) {
        if (!isFilmInfoValid(film)) {
            log.info("Валидация при сохранении нового фильма не пройдена!");
            throw new ValidationException("Информация о новом фильме не проходит условия валидации. Фильм не добавлен!");
        }

        Film newFilm = filmStorage.addFilm(film);

        if (newFilm.getGenres() != null) {
            filmStorage.batchFilmGenreInsert(newFilm.getId(), newFilm.getGenres());
            genreService.addGenresToFilms(List.of(newFilm));
        }

        mpaService.addMpaToFilms(List.of(newFilm));

        log.info(String.format("Новый фильм с ID = %d добавлен!", newFilm.getId()));
        return newFilm;
    }

    public Film updateFilm(Film film) {
        if (!isFilmInfoValid(film)) {
            log.info(String.format("Валидация при обновлении фильма с ID = %d не пройдена!", film.getId()));
            throw new ValidationException(String.format("Информация о фильме с ID = %d не проходит условия валидации. Фильм не добавлен!", film.getId()));
        } else if (!getFilms().contains(film)) {
            log.info(String.format("Попытка обновления несуществующего фильма с ID = %d!", film.getId()));
            throw new ObjectNotExistException(String.format("Попытка обновления несуществующего фильма с ID = %d!", film.getId()));
        }

        Film newFilm = filmStorage.updateFilm(film);

        if (newFilm.getGenres() != null) {
            filmStorage.batchFilmGenreInsert(newFilm.getId(), newFilm.getGenres());
            genreService.addGenresToFilms(List.of(newFilm));
            newFilm.getGenres().sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
        }

        mpaService.addMpaToFilms(List.of(newFilm));

        log.info(String.format("Фильм с ID = %d обновлен!", newFilm.getId()));
        return newFilm;
    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        genreService.addGenresToFilms(films);
        mpaService.addMpaToFilms(films);
        log.info("Запрос на получение списка фильмов getFilms выполнен!");
        return films;
    }

    public Film getFilmById(Long filmId) {
        Film film = filmStorage.getFilmById(filmId).orElseThrow(() -> {
            log.info(String.format(String.format("Фильм с ID = %d не найден!", filmId)));
            throw new ObjectNotExistException(String.format("Фильм с ID = %d не найден!", filmId));
        });

        genreService.addGenresToFilms(List.of(film));
        mpaService.addMpaToFilms(List.of(film));
        log.info(String.format(String.format("Фильм с ID = %d найден!", filmId)));
        return film;
    }

    public void addLike(Long userId, Long filmId) {
        filmStorage.addLike(userId, filmId);
        log.info(String.format(String.format("Пользователь с ID = %d поставил лайк фильму с ID = %d!", userId, filmId)));
    }

    public void deleteLike(Long userId, Long filmId) {
        filmStorage.deleteLike(userId, filmId);
        log.info(String.format(String.format("Пользователь с ID = %d удалил лайк фильму с ID = %d!", userId, filmId)));
    }

    public List<Film> getTopFilms(int count) {
        List<Film> result = getFilms().stream()
                .sorted(Comparator.comparingLong(f -> f.getRate() * -1))
                .limit(count)
                .collect(Collectors.toList());
        log.info("Запрос на получение списка ТОП-фильмов выполнен!");
        return result;
    }

    private boolean isFilmInfoValid(Film film) {
        return !film.getName().trim().isEmpty()
                && film.getDescription().length() <= 200
                && !film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                && film.getDuration() > 0;
    }
}
