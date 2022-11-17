package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotExistException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component("InMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private int id = 0;

    @Override
    public Film addFilm(Film film) {
        film.setId(++id);
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: " + film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        log.info("Обновлен фильм: " + film);
        return film;
    }

    @Override
    public Film deleteFilm(Film film) {
        films.remove(film.getId());
        log.info("Удалён фильм: " + film);
        return film;
    }

    public Optional<Film> getFilmById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    public void addLike(Long userId, Long filmId) {
        Film film = films.get(filmId);
        film.getUsersLiked().add(userId);
        film.setRate(film.getRate()+1);
    }

    public void deleteLike(Long userId, Long filmId) {
        Film film = films.get(filmId);
        if (film != null) {
            if (film.getUsersLiked().contains(userId)) {
                film.getUsersLiked().remove(userId);
            } else {
                throw new ObjectNotExistException("Лайк не найден!");
            }
        } else {
            throw new ObjectNotExistException("Фильм не найден!");
        }
    }
}
