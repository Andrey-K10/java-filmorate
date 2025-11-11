package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY mpa_id";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    public MpaRating getMpaRatingById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";
        List<MpaRating> mpaRatings = jdbcTemplate.query(sql, this::mapRowToMpa, id);

        if (mpaRatings.isEmpty()) {
            throw new NotFoundException("MPA рейтинг с id " + id + " не найден");
        }

        return mpaRatings.get(0);
    }

    private MpaRating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new MpaRating(rs.getInt("mpa_id"), rs.getString("name"));
    }
}