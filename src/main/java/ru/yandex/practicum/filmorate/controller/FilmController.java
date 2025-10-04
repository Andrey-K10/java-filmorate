package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов");
        List<Film> films = filmService.getAllFilms();
        log.info("Возвращено {} фильмов", films.size());
        return films;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        Film addedFilm = filmService.addFilm(film);
        log.info("Фильм успешно добавлен с id: {}", addedFilm.getId());
        return addedFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с id {}: {}", film.getId(), film);
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм с id {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        log.info("Получен запрос на получение фильма с id: {}", id);
        Film film = filmService.getFilmById(id);
        log.info("Найден фильм: {}", film);
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на добавление лайка фильму с id {} от пользователя с id {}", id, userId);
        filmService.addLike(id, userId);
        log.info("Лайк успешно добавлен фильму с id {} от пользователя с id {}", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на удаление лайка фильму с id {} от пользователя с id {}", id, userId);
        filmService.removeLike(id, userId);
        log.info("Лайк успешно удален у фильма с id {} от пользователя с id {}", id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение {} популярных фильмов", count);
        List<Film> popularFilms = filmService.getPopularFilms(count);
        log.info("Возвращено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

}