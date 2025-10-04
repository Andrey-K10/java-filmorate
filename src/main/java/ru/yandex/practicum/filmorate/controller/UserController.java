package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        List<User> users = userService.getAllUsers();
        log.info("Возвращено {} пользователей", users.size());
        return users;
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        User createdUser = userService.addUser(user);
        log.info("Пользователь успешно создан с id: {}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с id {}: {}", user.getId(), user);
        User updatedUser = userService.updateUser(user);
        log.info("Пользователь с id {} успешно обновлен", updatedUser.getId());
        return updatedUser;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        log.info("Получен запрос на получение пользователя с id: {}", id);
        User user = userService.getUserById(id);
        log.info("Найден пользователь: {}", user);
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Получен запрос на добавление в друзья: пользователь {} добавляет пользователя {}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Пользователь {} успешно добавлен в друзья пользователю {}", friendId, id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Получен запрос на удаление из друзей: пользователь {} удаляет пользователя {}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Пользователь {} успешно удален из друзей пользователя {}", friendId, id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        log.info("Получен запрос на получение списка друзей пользователя с id: {}", id);
        List<User> friends = userService.getFriends(id);
        log.info("Возвращено {} друзей пользователя с id {}", friends.size(), id);
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("Получен запрос на получение общих друзей пользователей {} и {}", id, otherId);
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.info("Найдено {} общих друзей пользователей {} и {}", commonFriends.size(), id, otherId);
        return commonFriends;
    }
}