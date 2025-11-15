package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public void addFriend(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        String checkSql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count != null && count > 0) {
            throw new ValidationException("Пользователь уже в друзьях");
        }

        String sql = "INSERT INTO friendships (user_id, friend_id, status_id) VALUES (?, ?, 1)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public List<User> getFriends(int userId) {
        userStorage.getUserById(userId);

        String sql = "SELECT u.* FROM friendships f " +
                "JOIN users u ON f.friend_id = u.user_id " +
                "WHERE f.user_id = ? AND f.status_id = 2";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId);
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(otherUserId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "AND f1.status_id = 2 AND f2.status_id = 2";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId, otherUserId);
    }

    public void confirmFriend(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        String checkSql = "SELECT status_id FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            Integer status = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);

            if (status == null || status != 1) {
                throw new ValidationException("Запрос на дружбу не найден");
            }

            String updateSql = "UPDATE friendships SET status_id = 2 WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
            jdbcTemplate.update(updateSql, userId, friendId, friendId, userId);
        } catch (Exception e) {
            throw new ValidationException("Запрос на дружбу не найден");
        }
    }
}