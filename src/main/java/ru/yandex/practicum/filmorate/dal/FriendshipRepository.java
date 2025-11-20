package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendshipRepository {

    private final JdbcTemplate jdbcTemplate;

    public void addFriend(int userId, int friendId) {
        String checkSql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count != null && count > 0) {
            throw new ValidationException("Пользователь уже в друзьях");
        }

        String sql = "INSERT INTO friendships (user_id, friend_id, status_id) VALUES (?, ?, 2)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        int deleted = jdbcTemplate.update(sql, userId, friendId);

        if (deleted == 0) {
            throw new ValidationException("Друг не найден");
        }
    }

    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM friendships f " +
                "JOIN users u ON f.friend_id = u.user_id " +
                "WHERE f.user_id = ? AND f.status_id = 2";

        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "AND f1.status_id = 2 AND f2.status_id = 2";

        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherUserId);
    }

    public void confirmFriend(int userId, int friendId) {
        String checkSql = "SELECT status_id FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            Integer status = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);

            if (status == null || status != 1) {
                throw new ValidationException("Запрос на дружбу не найден");
            }

            String updateSql = "UPDATE friendships SET status_id = 2 WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(updateSql, friendId, userId);

            String insertSql = "INSERT INTO friendships (user_id, friend_id, status_id) VALUES (?, ?, 2)";
            jdbcTemplate.update(insertSql, userId, friendId);

        } catch (Exception e) {
            throw new ValidationException("Запрос на дружбу не найден");
        }
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }
}