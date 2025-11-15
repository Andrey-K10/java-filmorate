package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    public void testFindFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film createdFilm = filmStorage.addFilm(film);
        Film foundFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(foundFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", createdFilm.getId())
                .hasFieldOrPropertyWithValue("name", "Test Film");
    }

    @Test
    public void testGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Test Film 1");
        film1.setDescription("Test Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);

        MpaRating mpa1 = new MpaRating();
        mpa1.setId(1);
        film1.setMpa(mpa1);

        Film film2 = new Film();
        film2.setName("Test Film 2");
        film2.setDescription("Test Description 2");
        film2.setReleaseDate(LocalDate.of(2005, 1, 1));
        film2.setDuration(150);

        MpaRating mpa2 = new MpaRating();
        mpa2.setId(2);
        film2.setMpa(mpa2);

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);

        List<Film> films = filmStorage.getAllFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film createdFilm = filmStorage.addFilm(film);
        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated Description");

        Film updatedFilm = filmStorage.updateFilm(createdFilm);

        assertThat(updatedFilm)
                .hasFieldOrPropertyWithValue("id", createdFilm.getId())
                .hasFieldOrPropertyWithValue("name", "Updated Film")
                .hasFieldOrPropertyWithValue("description", "Updated Description");
    }

    @Test
    public void testAddFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film createdFilm = filmStorage.addFilm(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isPositive();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getDescription()).isEqualTo("Test Description");
        assertThat(createdFilm.getDuration()).isEqualTo(120);
    }
}