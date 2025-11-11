package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate")
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(MpaRating.PG_13);
    }

    @Test
    void testAddFilm() {
        Film savedFilm = filmStorage.addFilm(testFilm);

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getId()).isPositive();
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
        assertThat(savedFilm.getDescription()).isEqualTo("Test Description");
        assertThat(savedFilm.getMpa()).isEqualTo(MpaRating.PG_13);
    }

    @Test
    void testUpdateFilm() {
        Film savedFilm = filmStorage.addFilm(testFilm);
        savedFilm.setName("Updated Film");
        savedFilm.setDescription("Updated Description");

        Film updatedFilm = filmStorage.updateFilm(savedFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void testGetFilmById() {
        Film savedFilm = filmStorage.addFilm(testFilm);
        Film foundFilm = filmStorage.getFilmById(savedFilm.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(savedFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo(savedFilm.getName());
    }

    @Test
    void testGetAllFilms() {
        Film film1 = filmStorage.addFilm(testFilm);

        Film film2 = new Film();
        film2.setName("Another Film");
        film2.setDescription("Another Description");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(90);
        film2.setMpa(MpaRating.G);
        filmStorage.addFilm(film2);

        List<Film> films = filmStorage.getAllFilms();

        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName)
                .contains("Test Film", "Another Film");
    }
}