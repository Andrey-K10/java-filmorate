package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Integer, Map<Integer, FriendshipStatus>> friendships = new HashMap<>();

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        log.debug("Получение всех пользователей из хранилища");
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        log.debug("Добавление нового пользователя: {}", user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User addedUser = userStorage.addUser(user);
        log.info("Пользователь успешно добавлен с id: {}", addedUser.getId());
        return addedUser;
    }

    public User updateUser(User user) {
        log.debug("Обновление пользователя с id {}: {}", user.getId(), user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User updatedUser = userStorage.updateUser(user);
        log.info("Пользователь с id {} успешно обновлен", updatedUser.getId());
        return updatedUser;
    }

    public User getUserById(int id) {
        log.debug("Поиск пользователя по id: {}", id);
        User user = userStorage.getUserById(id);
        log.debug("Найден пользователь: {}", user);
        return user;
    }

    // Добавление в друзья (неподтвержденная дружба)
    public void addFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        friendships.putIfAbsent(userId, new HashMap<>());
        friendships.putIfAbsent(friendId, new HashMap<>());

        if (friendships.get(userId).containsKey(friendId)) {
            log.warn("Пользователь с id {} уже в друзьях у пользователя с id {}", friendId, userId);
            throw new ValidationException("Пользователь уже в друзьях");
        }

        // Устанавливаем статус "неподтвержденная"
        friendships.get(userId).put(friendId, FriendshipStatus.PENDING);
        log.info("Пользователь с id {} отправил запрос на дружбу пользователю с id {}", userId, friendId);
    }

    // Подтверждение дружбы
    public void confirmFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        // Проверяем, что есть запрос на дружбу
        if (!friendships.containsKey(friendId) ||
                !friendships.get(friendId).containsKey(userId) ||
                friendships.get(friendId).get(userId) != FriendshipStatus.PENDING) {
            log.warn("Запрос на дружбу от пользователя {} к пользователю {} не найден", friendId, userId);
            throw new ValidationException("Запрос на дружбу не найден");
        }

        // Подтверждаем дружбу с обеих сторон
        friendships.get(friendId).put(userId, FriendshipStatus.CONFIRMED);
        friendships.get(userId).put(friendId, FriendshipStatus.CONFIRMED);
        log.info("Пользователь с id {} подтвердил дружбу с пользователем с id {}", userId, friendId);
    }

    // Удаление из друзей
    public void removeFriend(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        if (friendships.containsKey(userId)) {
            friendships.get(userId).remove(friendId);
        }
        if (friendships.containsKey(friendId)) {
            friendships.get(friendId).remove(userId);
        }
        log.info("Пользователи с id {} и {} больше не друзья", userId, friendId);
    }

    // Получение списка друзей (только подтвержденные)
    public List<User> getFriends(int userId) {
        userStorage.getUserById(userId);
        Map<Integer, FriendshipStatus> userFriends = friendships.getOrDefault(userId, Collections.emptyMap());
        return userFriends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    // Получение заявок в друзья (неподтвержденные)
    public List<User> getFriendRequests(int userId) {
        userStorage.getUserById(userId);
        Map<Integer, FriendshipStatus> userFriends = friendships.getOrDefault(userId, Collections.emptyMap());
        return userFriends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.PENDING)
                .map(Map.Entry::getKey)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    // Получение общих друзей (только подтвержденные)
    public List<User> getCommonFriends(int userId, int otherUserId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(otherUserId);

        Map<Integer, FriendshipStatus> userFriends = friendships.getOrDefault(userId, Collections.emptyMap());
        Map<Integer, FriendshipStatus> otherUserFriends = friendships.getOrDefault(otherUserId, Collections.emptyMap());

        Set<Integer> userConfirmedFriends = userFriends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<Integer> otherUserConfirmedFriends = otherUserFriends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return userConfirmedFriends.stream()
                .filter(otherUserConfirmedFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }
}