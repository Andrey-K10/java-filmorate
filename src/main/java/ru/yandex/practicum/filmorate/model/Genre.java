package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Genre {
    private int id;
    private String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Можно создать предопределенные жанры
    public static final Genre COMEDY = new Genre(1, "Комедия");
    public static final Genre DRAMA = new Genre(2, "Драма");
    public static final Genre CARTOON = new Genre(3, "Мультфильм");
    public static final Genre THRILLER = new Genre(4, "Триллер");
    public static final Genre DOCUMENTARY = new Genre(5, "Документальный");
    public static final Genre ACTION = new Genre(6, "Боевик");
}