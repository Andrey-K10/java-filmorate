package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User user) {
        log.info("Добавление пользователя: {}", user);
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен с id: {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        log.info("Обновление пользователя с id {}: {}", user.getId(), user);
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id {} не найден", user.getId());
            throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Пользователь с id {} успешно обновлен", user.getId());
        return user;
    }

    @Override
    public User getUserById(int id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }
}