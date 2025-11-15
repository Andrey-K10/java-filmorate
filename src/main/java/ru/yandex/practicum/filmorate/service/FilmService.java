package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import java.util.*;
import java.time.LocalDate;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        validateMpaExists(film.getMpa().getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                validateGenreExists(genre.getId());
            }
        }
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        validateMpaExists(film.getMpa().getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                validateGenreExists(genre.getId());
            }
        }
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }

    public void addLike(int filmId, int userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (Exception e) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(sql, filmId, userId);

        if (deleted == 0) {
            throw new ValidationException("Лайк не найден");
        }
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }

        String sql = "SELECT f.*, m.mpa_id, m.name as mpa_name, COUNT(fl.user_id) as likes_count " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> popularFilms = jdbcTemplate.query(sql, (rs, rowNum) -> {
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
        }, count);

        for (Film film : popularFilms) {
            loadFilmGenres(film);
        }

        return popularFilms;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза не может быть пустой");
        }

        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private void validateMpaExists(int mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE mpa_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpaId);
        if (count == null || count == 0) {
            throw new NotFoundException("MPA рейтинг с id " + mpaId + " не найден");
        }
    }

    private void validateGenreExists(int genreId) {
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreId);
        if (count == null || count == 0) {
            throw new NotFoundException("Жанр с id " + genreId + " не найден");
        }
    }

    private void loadFilmGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new Genre(rs.getInt("genre_id"), rs.getString("name"));
        }, film.getId());

        film.setGenres(new HashSet<>(genres));
    }
}