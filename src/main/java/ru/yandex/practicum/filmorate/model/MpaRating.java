package ru.yandex.practicum.filmorate.model;

public enum MpaRating {
    G("G"),
    PG("PG"),
    PG_13("PG-13"),
    R("R"),
    NC_17("NC-17");

    private final String title;

    MpaRating(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}