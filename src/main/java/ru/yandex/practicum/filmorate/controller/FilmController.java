package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final List<Film> films = new ArrayList<>();
    private int nextId = 1;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    // Добавление фильма
    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        try {
            validateFilm(film);
            film.setId(nextId++);
            films.add(film);
            return film;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при добавлении фильма: {}", e.getMessage());
            throw e;
        }

    }

    // Обновление фильма
    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с id {}: {}", film.getId(), film);
        try {
            validateFilm(film);
            for (int i = 0; i < films.size(); i++) {
                if (films.get(i).getId() == film.getId()) {
                    films.set(i, film);
                    log.info("Фильм с id {} успешно обновлен: {}", film.getId(), film);
                    return film;
                }
            }
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        } catch (ValidationException e) {
            log.warn("Ошибка при обновлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    // Получение всех фильмов
    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов. Текущее количество: {}", films.size());
        return films;
    }

    // Валидация
    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.debug("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.debug("Валидация не пройдена: описание слишком длинное ({} символов)", film.getDescription().length());
            throw new ValidationException("Описание не может быть длиннее " + MAX_DESCRIPTION_LENGTH + " символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.debug("Валидация не пройдена: неверная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }

        if (film.getDuration() <= 0) {
            log.debug("Валидация не пройдена: неверная продолжительность {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        log.debug("Валидация фильма пройдена успешно");
    }
}