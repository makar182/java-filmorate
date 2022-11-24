package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Mpa {
    private Long id;
    private String name;

    public Mpa(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Mpa(long id) {
        this.id = id;
    }
}
