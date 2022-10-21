package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film deleteFilm(Film film);

    List<Film> getFilms();

    Film getFilmById(Long filmId);

    void addLike(Long userId, Long filmId);

    void deleteLike(Long userId, Long filmId);
}
