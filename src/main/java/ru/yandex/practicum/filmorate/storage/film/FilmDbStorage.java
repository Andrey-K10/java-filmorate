package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.sql.*;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@Primary
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, m.mpa_id, m.name as mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film addFilm(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == 0) {
            throw new ValidationException("MPA рейтинг не может быть null");
        }

        validateMpaExists(film.getMpa().getId());

        String sql = "INSERT INTO films (title, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validateMpaExists(film.getMpa().getId());

        String sql = "UPDATE films SET title = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (updated == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        return film;
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT f.*, m.mpa_id, m.name as mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id WHERE film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);

        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        return films.get(0);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("title"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        return film;
    }

    private void validateMpaExists(int mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE mpa_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpaId);
        if (count == null || count == 0) {
            throw new NotFoundException("MPA рейтинг с id " + mpaId + " не найден");
        }
    }
}