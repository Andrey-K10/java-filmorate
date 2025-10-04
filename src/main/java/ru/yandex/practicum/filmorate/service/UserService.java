package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;
import java.util.stream.Collectors;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> friendships = new HashMap<>(); // userId -> friendIds

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        // Если имя пустое, используем логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        // Если имя пустое, используем логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    // Добавление в друзья
    public void addFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        friendships.putIfAbsent(userId, new HashSet<>());
        friendships.putIfAbsent(friendId, new HashSet<>());

        if (friendships.get(userId).contains(friendId)) {
            log.warn("Пользователь с id {} уже в друзьях у пользователя с id {}", friendId, userId);
            throw new ValidationException("Пользователь уже в друзьях");
        }

        friendships.get(userId).add(friendId);
        friendships.get(friendId).add(userId); // взаимная дружба
        log.info("Пользователи с id {} и {} теперь друзья", userId, friendId);
    }

    // Удаление из друзей
    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (!friendships.containsKey(userId) || !friendships.get(userId).contains(friendId)) {
            log.warn("Пользователь с id {} не в друзьях у пользователя с id {}", friendId, userId);
            throw new NotFoundException("Пользователь не в друзьях"); //
        }

        friendships.get(userId).remove(friendId);
        friendships.get(friendId).remove(userId);
        log.info("Пользователи с id {} и {} больше не друзья", userId, friendId);
    }

    // Получение списка друзей
    public List<User> getFriends(int userId) {
        userStorage.getUserById(userId); // проверяем существование пользователя
        return friendships.getOrDefault(userId, Collections.emptySet()).stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    // Получение общих друзей
    public List<User> getCommonFriends(int userId, int otherUserId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(otherUserId);

        Set<Integer> userFriends = friendships.getOrDefault(userId, Collections.emptySet());
        Set<Integer> otherUserFriends = friendships.getOrDefault(otherUserId, Collections.emptySet());

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }
}