package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.*;
import java.time.LocalDate;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllFilms() {
        log.debug("Получение всех фильмов из хранилища");
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film film) {
        log.debug("Добавление нового фильма: {}", film);
        validateFilm(film);
        Film addedFilm = filmStorage.addFilm(film);
        log.info("Фильм успешно добавлен с id: {}", addedFilm.getId());
        return addedFilm;
    }

    public Film updateFilm(Film film) {
        log.debug("Обновление фильма с id {}: {}", film.getId(), film);
        validateFilm(film);
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Фильм с id {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    public Film getFilmById(int id) {
        log.debug("Поиск фильма по id: {}", id);
        Film film = filmStorage.getFilmById(id);
        log.debug("Найден фильм: {}", film);
        return film;
    }

    public void addLike(int filmId, int userId) {
        log.debug("Добавление лайка фильму с id {} от пользователя с id {}", filmId, userId);

        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
            log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, filmId);
        } catch (Exception e) {
            log.warn("Пользователь с id {} уже поставил лайк фильму с id {}", userId, filmId);
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }
    }

    public void removeLike(int filmId, int userId) {
        log.debug("Удаление лайка фильму с id {} от пользователя с id {}", filmId, userId);

        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(sql, filmId, userId);

        if (deleted == 0) {
            log.warn("Лайк пользователя с id {} фильму с id {} не найден", userId, filmId);
            throw new ValidationException("Лайк не найден");
        }

        log.info("Пользователь с id {} удалил лайк с фильма с id {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("Получение {} популярных фильмов", count);

        if (count <= 0) {
            log.warn("Запрошено недопустимое количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }

        String sql = "SELECT f.*, m.name as mpa_name, COUNT(fl.user_id) as likes_count " +
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

        log.debug("Найдено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    private void validateFilm(Film film) {
        log.debug("Валидация фильма: {}", film);

        if (film.getReleaseDate() == null) {
            log.warn("Дата релиза фильма не может быть null");
            throw new ValidationException("Дата релиза не может быть пустой");
        }

        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Дата релиза {} раньше минимальной допустимой {}", film.getReleaseDate(), minReleaseDate);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        log.debug("Валидация фильма пройдена успешно");
    }

    public void addGenreToFilm(int filmId, Genre genre) {
        Film film = filmStorage.getFilmById(filmId);
        film.getGenres().add(genre);
        log.info("Жанр {} добавлен к фильму с id {}", genre.getName(), filmId);
    }

    public void removeGenreFromFilm(int filmId, Genre genre) {
        Film film = filmStorage.getFilmById(filmId);
        film.getGenres().remove(genre);
        log.info("Жанр {} удален из фильма с id {}", genre.getName(), filmId);
    }

    public void setFilmMpa(int filmId, MpaRating mpa) {
        Film film = filmStorage.getFilmById(filmId);
        film.setMpa(mpa);
        log.info("MPA рейтинг {} установлен для фильма с id {}", mpa.getName(), filmId);
    }

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private void loadFilmGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new Genre(rs.getInt("genre_id"), rs.getString("name"));
        }, film.getId());

        film.setGenres(new HashSet<>(genres));
    }
}