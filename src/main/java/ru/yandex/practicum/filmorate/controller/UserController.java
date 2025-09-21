package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final List<User> users = new ArrayList<>();
    private int nextId = 1;

    // Создание пользователя
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        try {
            validateUser(user);
            // Если имя пустое, используем логин
            if (user.getName() == null || user.getName().isBlank()) {
                log.debug("Имя пользователя пустое, используем логин: {}", user.getLogin());
                user.setName(user.getLogin());
            }
            user.setId(nextId++);
            users.add(user);
            log.info("Пользователь успешно создан: {}", user);
            return user;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при создании пользователя: {}", e.getMessage());
            throw e;
        }
    }

    // Обновление пользователя
    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с id {}: {}", user.getId(), user);

        // Если имя пустое, используем логин
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя пустое, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                users.set(i, user);
                log.info("Пользователь с id {} успешно обновлен: {}", user.getId(), user);
                return user;
            }
        }

        log.warn("Пользователь с id {} не найден", user.getId());
        throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
    }

    // Получение списка всех пользователей
    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users;
    }

    // Валидация
    private void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.debug("Валидация не пройдена: логин содержит пробелы - {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        } // Оставил, так как не нашел какую аннотацию лучше применить для пробелов

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("Валидация не пройдена: дата рождения в будущем - {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        } // Оставил, так как не нашел какую аннотацию лучше применить для дат

        log.debug("Валидация пользователя пройдена успешно");
    }

}