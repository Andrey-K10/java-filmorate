package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = createValidFilm();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = createValidFilm();
        film.setName("   ");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenDescriptionIsTooLong() {
        Film film = createValidFilm();
        film.setDescription("A".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Описание не может быть длиннее 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenReleaseDateIsNull() {
        Film film = createValidFilm();
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Дата релиза не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = createValidFilm();
        film.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }
}