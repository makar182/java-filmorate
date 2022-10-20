package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private int id = 0;

    @Override
    public Film addFilm(Film film) {
        if (!isFilmInfoValid(film)) {
            log.debug("Валидация при сохранении фильма не пройдена!");
            throw new ValidationException("Информация о фильме не проходит условия валидации. Фильм не добавлен!");
        }
        film.setId(++id);
        films.put(film.getId(), film);
        log.debug("Добавлен новый фильм: " + film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!isFilmInfoValid(film)) {
            log.debug("Валидация при обновлении фильма не пройдена!");
            throw new ValidationException("Информация о фильме не проходит условия валидации. Фильм не добавлен!");
        } else if (!films.containsKey(film.getId())) {
            log.debug("Попытка обновления несуществующего фильма!");
            throw new ObjectNotExistException("Попытка обновления несуществующего фильма!");
        }
        films.put(film.getId(), film);
        log.debug("Обновлен фильм: " + film);
        return film;
    }

    @Override
    public Film deleteFilm(Film film) {
        films.remove(film.getId());
        log.debug("Удалён фильм: " + film);
        return film;
    }

    public Film getFilmById(Long filmId) {
        return films.get(filmId);
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    public void addLike(Long userId, Long filmId) {
        films.get(filmId).addLike(userId);
    }

    public void deleteLike(Long userId, Long filmId) {
        Film film = films.get(filmId);
        if (film.getUsersLiked().contains(userId)) {
            film.getUsersLiked().remove(userId);
        } else {
            throw new ObjectNotExistException("Лайк не найден!");
        }
    }

    private boolean isFilmInfoValid(Film film) {
        return !film.getName().trim().isEmpty()
                && film.getDescription().length() <= 200
                && !film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                && film.getDuration() > 0;
    }
}
