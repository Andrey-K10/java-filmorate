package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void addFilm_WithValidData_ShouldSuccess() {
        Film result = filmController.addFilm(validFilm);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Valid Film", result.getName());
    }

    @Test
    void addFilm_WithEmptyName_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void addFilm_WithNullName_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void addFilm_WithLongDescription_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(201)); // 201 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void addFilm_WithExactly200SymbolDescription_ShouldSuccess() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(200)); // Ровно 200 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film result = filmController.addFilm(film);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void addFilm_WithEarlyReleaseDate_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // На день раньше
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void addFilm_WithMinimalReleaseDate_ShouldSuccess() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // Минимальная дата
        film.setDuration(120);

        Film result = filmController.addFilm(film);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void addFilm_WithNegativeDuration_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void addFilm_WithZeroDuration_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void addFilm_WithNullReleaseDate_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(null);
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }
}