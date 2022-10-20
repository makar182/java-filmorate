package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final InMemoryFilmStorage inMemoryFilmStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage inMemoryFilmStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }

    public Film addFilm(Film film) {
        if (!isFilmInfoValid(film)) {
            log.debug("Валидация при сохранении фильма не пройдена!");
            throw new ValidationException("Информация о фильме не проходит условия валидации. Фильм не добавлен!");
        }
        return inMemoryFilmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (!isFilmInfoValid(film)) {
            log.debug("Валидация при обновлении фильма не пройдена!");
            throw new ValidationException("Информация о фильме не проходит условия валидации. Фильм не добавлен!");
        } else if (!getFilms().contains(film)) {
            log.debug("Попытка обновления несуществующего фильма!");
            throw new ObjectNotExistException("Попытка обновления несуществующего фильма!");
        }
        return inMemoryFilmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return inMemoryFilmStorage.getFilms();
    }

    public Film getFilmById(Long filmId) {
        Film film = inMemoryFilmStorage.getFilmById(filmId);
        if (film == null) {
            throw new ObjectNotExistException(String.format("Фильм №%d не найден!", filmId));
        }
        return film;
    }

    public void addLike(Long userId, Long filmId) {
        inMemoryFilmStorage.addLike(userId, filmId);
    }

    public void deleteLike(Long userId, Long filmId) {
        inMemoryFilmStorage.deleteLike(userId, filmId);
    }

    public List<Film> getTopFilms(int count) {
        List<Film> films = inMemoryFilmStorage.getFilms();
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
