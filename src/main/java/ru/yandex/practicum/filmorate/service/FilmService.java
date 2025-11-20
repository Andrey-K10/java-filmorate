package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.dal.FilmLikesRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.ValidationRepository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmLikesRepository filmLikesRepository;
    private final GenreRepository genreRepository;
    private final ValidationRepository validationRepository;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       FilmLikesRepository filmLikesRepository,
                       GenreRepository genreRepository,
                       ValidationRepository validationRepository) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.filmLikesRepository = filmLikesRepository;
        this.genreRepository = genreRepository;
        this.validationRepository = validationRepository;
    }

    public List<Film> getAllFilms() {
        log.debug("Получение всех фильмов из хранилища");
        List<Film> films = filmStorage.getAllFilms();
        genreRepository.loadGenresForFilms(films);
        return films;
    }

    public Film addFilm(Film film) {
        log.debug("Добавление нового фильма: {}", film);
        validateFilm(film);

        validationRepository.validateMpaExists(film.getMpa().getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            validationRepository.validateGenresExist(genreIds);
        }

        Film addedFilm = filmStorage.addFilm(film);
        genreRepository.saveFilmGenres(addedFilm.getId(), addedFilm.getGenres());
        log.info("Фильм успешно добавлен с id: {}", addedFilm.getId());
        return addedFilm;
    }

    public Film updateFilm(Film film) {
        log.debug("Обновление фильма с id {}: {}", film.getId(), film);
        validateFilm(film);

        validationRepository.validateMpaExists(film.getMpa().getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            validationRepository.validateGenresExist(genreIds);
        }

        Film updatedFilm = filmStorage.updateFilm(film);
        genreRepository.saveFilmGenres(updatedFilm.getId(), updatedFilm.getGenres());
        log.info("Фильм с id {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    public Film getFilmById(int id) {
        log.debug("Поиск фильма по id: {}", id);
        Film film = filmStorage.getFilmById(id);
        genreRepository.loadGenresForFilm(film);
        log.debug("Найден фильм: {}", film);
        return film;
    }

    public void addLike(int filmId, int userId) {
        log.debug("Добавление лайка фильму с id {} от пользователя с id {}", filmId, userId);
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        filmLikesRepository.addLike(filmId, userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        log.debug("Удаление лайка фильму с id {} от пользователя с id {}", filmId, userId);
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        filmLikesRepository.removeLike(filmId, userId);
        log.info("Пользователь с id {} удалил лайк с фильма с id {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("Получение {} популярных фильмов", count);

        if (count <= 0) {
            log.warn("Запрошено недопустимое количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }

        List<Film> popularFilms = filmLikesRepository.getPopularFilms(count);
        genreRepository.loadGenresForFilms(popularFilms);
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
}