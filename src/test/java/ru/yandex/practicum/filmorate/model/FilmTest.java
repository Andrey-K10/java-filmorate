package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.controller.FilmController;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private Validator validator;
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        filmController = new FilmController();
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("A valid film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации для корректного фильма");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenNameIsNull() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenDescriptionIsTooLong() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(201)); // 201 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Описание не может быть длиннее 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    void shouldAcceptDescriptionWithMaxLength() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(200)); // Ровно 200 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Должно принимать описание длиной 200 символов");
    }

    @Test
    void shouldFailWhenReleaseDateIsNull() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(null);
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Дата релиза не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenDurationIsZero() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenReleaseDateIsBeforeMinDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // За день до минимальной даты
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void shouldAcceptReleaseDateExactlyMinDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // Точная минимальная дата
        film.setDuration(120);

        // Проверяем через контроллер, так как это кастомная валидация
        Film addedFilm = filmController.addFilm(film);
        assertNotNull(addedFilm);
        // Не проверяем конкретный ID, так как он зависит от порядка выполнения тестов
        assertTrue(addedFilm.getId() > 0);
    }

    @Test
    void shouldAcceptReleaseDateAfterMinDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 29)); // На день позже минимальной даты
        film.setDuration(120);

        Film addedFilm = filmController.addFilm(film);
        assertNotNull(addedFilm);
        // Не проверяем конкретный ID, так как он зависит от порядка выполнения тестов
        assertTrue(addedFilm.getId() > 0);
    }

    @Test
    void shouldHandleMultipleValidationErrors() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(null);
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(4, violations.size());
    }
}