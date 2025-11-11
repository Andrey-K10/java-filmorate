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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.HashSet;

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
        String sql = "SELECT f.*, m.name as mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);

        // Загружаем жанры для каждого фильма
        for (Film film : films) {
            loadFilmGenres(film);
        }

        return films;
    }

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO films (title, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, mapMpaEnumToId(film.getMpa()));
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        saveFilmGenres(film);
        log.info("Фильм добавлен в БД с id: {}", film.getId());
        return film;
    }

    private int mapMpaEnumToId(MpaRating mpa) {
        if (mpa == null) {
            throw new ValidationException("MPA рейтинг не может быть null");
        }
        switch (mpa) {
            case G: return 1;
            case PG: return 2;
            case PG_13: return 3;
            case R: return 4;
            case NC_17: return 5;
            default: throw new ValidationException("Неизвестный MPA рейтинг: " + mpa);
        }
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET title = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mapMpaEnumToId(film.getMpa()),
                film.getId()
        );

        if (updated == 0) {
            log.warn("Фильм с id {} не найден при обновлении", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        updateFilmGenres(film);

        log.info("Фильм с id {} обновлен в БД", film.getId());
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT f.*, m.name as mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id WHERE film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);

        if (films.isEmpty()) {
            log.warn("Фильм с id {} не найден в БД", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        Film film = films.get(0);
        loadFilmGenres(film);

        return film;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("title"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        // Устанавливаем MPA рейтинг
        String mpaName = rs.getString("mpa_name");
        MpaRating mpaRating = mapMpaNameToEnum(mpaName);
        film.setMpa(mpaRating);

        return film;
    }

    private void saveFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        // Добавляем новые жанры
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().forEach(genre -> {
                jdbcTemplate.update(insertSql, film.getId(), genre.getId());
            });
        }
    }

    private void updateFilmGenres(Film film) {
        saveFilmGenres(film);
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new Genre(rs.getInt("genre_id"), rs.getString("name"));
        }, film.getId());

        film.setGenres(new HashSet<>(genres));
    }

    private MpaRating mapMpaNameToEnum(String mpaName) {
        switch (mpaName) {
            case "G": return MpaRating.G;
            case "PG": return MpaRating.PG;
            case "PG-13": return MpaRating.PG_13;
            case "R": return MpaRating.R;
            case "NC-17": return MpaRating.NC_17;
            default: throw new IllegalArgumentException("Unknown MPA rating: " + mpaName);
        }
    }
}