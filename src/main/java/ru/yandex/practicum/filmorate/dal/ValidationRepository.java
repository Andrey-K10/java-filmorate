package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import java.util.Collections;
import java.util.Set;


@Repository
@RequiredArgsConstructor
public class ValidationRepository {

    private final JdbcTemplate jdbcTemplate;

    public void validateMpaExists(int mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE mpa_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpaId);
        if (count == null || count == 0) {
            throw new NotFoundException("MPA рейтинг с id " + mpaId + " не найден");
        }
    }

    public void validateGenresExist(Set<Integer> genreIds) {
        if (genreIds.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(genreIds.size(), "?"));
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id IN (" + placeholders + ")";
        Integer count = jdbcTemplate.queryForObject(sql, genreIds.toArray(), Integer.class);

        if (count == null || count != genreIds.size()) {
            throw new NotFoundException("Некоторые жанры не найдены");
        }
    }
}