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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film addFilm(Film film) {
        if (!isFilmInfoValid(film)) {
            log.info("Валидация при сохранении нового фильма не пройдена!");
            throw new ValidationException("Информация о новом фильме не проходит условия валидации. Фильм не добавлен!");
        }
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (!isFilmInfoValid(film)) {
            log.info(String.format("Валидация при обновлении фильма №%d не пройдена!", film.getId()));
            throw new ValidationException(String.format("Информация о фильме №%d не проходит условия валидации. Фильм не добавлен!", film.getId()));
        } else if (!getFilms().contains(film)) {
            log.info(String.format("Попытка обновления несуществующего фильма №%d!", film.getId()));
            throw new ObjectNotExistException(String.format("Попытка обновления несуществующего фильма №%d!", film.getId()));
        }
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            log.info(String.format(String.format("Фильм №%d не найден!", filmId)));
            throw new ObjectNotExistException(String.format("Фильм №%d не найден!", filmId));
        }
        return film;
    }

    public void addLike(Long userId, Long filmId) {
        filmStorage.addLike(userId, filmId);
    }

    public void deleteLike(Long userId, Long filmId) {
        filmStorage.deleteLike(userId, filmId);
    }

    public List<Film> getTopFilms(int count) {
        List<Film> films = filmStorage.getFilms();
        return films.stream().sorted(Comparator.comparingInt(f -> f.getUsersLiked().size()))
                .sorted(Comparator.comparingInt(f -> f.getUsersLiked().size() * -1))
                .limit(count)
                .collect(Collectors.toList());
    }

    private boolean isFilmInfoValid(Film film) {
        return !film.getName().trim().isEmpty()
                && film.getDescription().length() <= 200
                && !film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                && film.getDuration() > 0;
    }
}
