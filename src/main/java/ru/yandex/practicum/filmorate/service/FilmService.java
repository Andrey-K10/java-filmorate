package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
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

        // Проверяем существование фильма и пользователя
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        likes.putIfAbsent(filmId, new HashSet<>());

        if (likes.get(filmId).contains(userId)) {
            log.warn("Пользователь с id {} уже поставил лайк фильму с id {}", userId, filmId);
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        likes.get(filmId).add(userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {}. Всего лайков: {}",
                userId, filmId, likes.get(filmId).size());
    }

    public void removeLike(int filmId, int userId) {
        log.debug("Удаление лайка фильму с id {} от пользователя с id {}", filmId, userId);

        // Проверяем существование фильма и пользователя
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        if (!likes.containsKey(filmId) || !likes.get(filmId).contains(userId)) {
            log.warn("Лайк пользователя с id {} фильму с id {} не найден", userId, filmId);
            throw new ValidationException("Лайк не найден");
        }

        likes.get(filmId).remove(userId);
        log.info("Пользователь с id {} удалил лайк с фильма с id {}. Осталось лайков: {}",
                userId, filmId, likes.get(filmId).size());
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("Получение {} популярных фильмов", count);

        if (count <= 0) {
            log.warn("Запрошено недопустимое количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }

        List<Film> popularFilms = filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> {
                    int likes1 = likes.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    int likes2 = likes.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Integer.compare(likes2, likes1); // сортировка по убыванию
                })
                .limit(count)
                .collect(Collectors.toList());

        log.debug("Найдено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    public int getLikesCount(int filmId) {
        int count = likes.getOrDefault(filmId, Collections.emptySet()).size();
        log.debug("Количество лайков у фильма с id {}: {}", filmId, count);
        return count;
    }

    private void validateFilm(Film film) {
        log.debug("Валидация фильма: {}", film);

        if (film.getReleaseDate() == null) {
            log.warn("Дата релиза фильма не может быть null");
            throw new ValidationException("Дата релиза не может быть пустой");
        }

        // Проверка минимальной даты релиза (28 декабря 1895)
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Дата релиза {} раньше минимальной допустимой {}", film.getReleaseDate(), minReleaseDate);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        log.debug("Валидация фильма пройдена успешно");
    }
}