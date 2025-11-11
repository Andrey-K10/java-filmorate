package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {

    private final MpaDbStorage mpaStorage;

    @GetMapping
    public List<MpaRating> getAllMpaRatings() {
        log.info("Получен запрос на получение всех MPA рейтингов");
        return mpaStorage.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaRatingById(@PathVariable int id) {
        log.info("Получен запрос на получение MPA рейтинга с id: {}", id);
        return mpaStorage.getMpaRatingById(id);
    }
}