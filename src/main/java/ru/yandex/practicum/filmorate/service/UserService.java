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

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        friendships.putIfAbsent(userId, new HashMap<>());

        if (friendships.get(userId).containsKey(friendId)) {
            log.warn("Пользователь с id {} уже в друзьях у пользователя с id {}", friendId, userId);
            throw new ValidationException("Пользователь уже в друзьях");
        }

        // Односторонняя дружба
        friendships.get(userId).put(friendId, FriendshipStatus.CONFIRMED);
        log.info("Пользователь с id {} добавил пользователя с id {} в друзья", userId, friendId);
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

        // Удаляем дружбу с обеих сторон
        if (friendships.containsKey(userId)) {
            friendships.get(userId).remove(friendId);
        }
        if (friendships.containsKey(friendId)) {
            friendships.get(friendId).remove(userId);
        }

        log.info("Пользователи с id {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        userStorage.getUserById(userId);

        if (!friendships.containsKey(userId)) {
            return new ArrayList<>();
        }

        Map<Integer, FriendshipStatus> userFriends = friendships.get(userId);
        return userFriends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .map(userStorage::getUserById)
                .filter(Objects::nonNull)
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

    public List<User> getCommonFriends(int userId, int otherUserId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(otherUserId);

        Set<Integer> userFriends = friendships.getOrDefault(userId, Collections.emptyMap())
                .entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<Integer> otherUserFriends = friendships.getOrDefault(otherUserId, Collections.emptyMap())
                .entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}