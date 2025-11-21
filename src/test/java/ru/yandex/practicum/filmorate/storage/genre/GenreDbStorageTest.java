package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class})
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    public void testGetAllGenres() {
        List<Genre> genres = genreStorage.getAllGenres();

        assertThat(genres).hasSize(6);
        assertThat(genres.get(0)).hasFieldOrPropertyWithValue("id", 1);
        assertThat(genres.get(0)).hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    public void testGetGenreById() {
        Genre genre = genreStorage.getGenreById(1);

        assertThat(genre)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }
}