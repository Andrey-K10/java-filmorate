package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class})
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    public void testGetAllMpaRatings() {
        List<MpaRating> mpaRatings = mpaStorage.getAllMpaRatings();

        assertThat(mpaRatings).hasSize(5);
        assertThat(mpaRatings.get(0)).hasFieldOrPropertyWithValue("id", 1);
        assertThat(mpaRatings.get(0)).hasFieldOrPropertyWithValue("name", "G");
    }

    @Test
    public void testGetMpaRatingById() {
        MpaRating mpaRating = mpaStorage.getMpaRatingById(1);

        assertThat(mpaRating)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
    }
}