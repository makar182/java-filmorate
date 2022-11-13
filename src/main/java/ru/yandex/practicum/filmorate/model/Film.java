package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.FilmGenre;
import ru.yandex.practicum.filmorate.enums.FilmRating;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Builder
public class Film {
    @NotNull
    private long id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    private int duration;

    private int rate;

    @NotNull
    private Mpa mpa;

    private Set<Long> usersLiked;

    private Set<Genre> genres;

    public void addLike(Long userId) {
        usersLiked.add(userId);
    }

    public void deleteLike(Long userId) {
        usersLiked.remove(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return id == film.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
