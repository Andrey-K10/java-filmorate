package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов. Текущее количество: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Film addFilm(Film film) {
        log.info("Добавление фильма: {}", film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с id: {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Обновление фильма с id {}: {}", film.getId(), film);
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("Фильм с id {} успешно обновлен", film.getId());
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }
}