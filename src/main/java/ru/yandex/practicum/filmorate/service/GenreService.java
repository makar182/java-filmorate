package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dbGenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class GenreService {
    private final dbGenreStorage genreDbStorage;

    public GenreService(@Qualifier("GenreDbStorage") dbGenreStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }

    public List<Genre> getGenres() {
        return genreDbStorage.getGenres();
    }

    public Genre getGenreById(long genreId) {
        return genreDbStorage.getGenreById(genreId);
    }

    public Genre makeGenre(ResultSet rs) throws SQLException {
        Genre genre = getGenreById(rs.getLong("ID"));
        return Genre.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}
