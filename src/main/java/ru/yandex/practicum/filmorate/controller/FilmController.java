package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private int id = 0;

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        if (!isFilmInfoValid(film)) {
            log.debug("Валидация при сохранении фильма не пройдена!");
            throw new ValidationException("Информация о фильме не проходит условия валидации. Фильм не добавлен!");
        }
        film.setId(++id);
        films.put(film.getId(), film);
        log.debug("Добавлен новый фильм: " + film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (!isFilmInfoValid(film)) {
            log.debug("Валидация при обновлении фильма не пройдена!");
            throw new ValidationException("Информация о фильме не проходит условия валидации. Фильм не добавлен!");
        } else if (!films.containsKey(film.getId())) {
            log.debug("Попытка обновления несуществующего фильма!");
            throw new ValidationException("Попытка обновления несуществующего фильма!");
        }
        films.put(film.getId(), film);
        log.debug("Обновлен фильм: " + film);
        return film;
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    private boolean isFilmInfoValid(Film film) {
        return !film.getName().trim().isEmpty()
                && film.getDescription().length() <= 200
                && !film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                && film.getDuration() > 0;
    }
}
