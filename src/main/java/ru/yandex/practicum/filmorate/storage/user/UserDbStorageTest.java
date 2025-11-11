package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate")
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@mail.com");
        testUser.setLogin("testuser");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testAddUser() {
        User savedUser = userStorage.addUser(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isPositive();
        assertThat(savedUser.getEmail()).isEqualTo("test@mail.com");
        assertThat(savedUser.getLogin()).isEqualTo("testuser");
        assertThat(savedUser.getName()).isEqualTo("Test User");
    }

    @Test
    void testUpdateUser() {
        User savedUser = userStorage.addUser(testUser);
        savedUser.setName("Updated Name");
        savedUser.setEmail("updated@mail.com");

        User updatedUser = userStorage.updateUser(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@mail.com");
    }

    @Test
    void testUpdateNonExistentUser() {
        testUser.setId(999);

        assertThrows(NotFoundException.class, () -> userStorage.updateUser(testUser));
    }

    @Test
    void testGetUserById() {
        User savedUser = userStorage.addUser(testUser);
        User foundUser = userStorage.getUserById(savedUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void testGetNonExistentUserById() {
        assertThrows(NotFoundException.class, () -> userStorage.getUserById(999));
    }

    @Test
    void testGetAllUsers() {
        User user1 = userStorage.addUser(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 5, 15));
        userStorage.addUser(user2);

        List<User> users = userStorage.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .contains("test@mail.com", "user2@mail.com");
    }

    @Test
    void testUserWithEmptyNameUsesLogin() {
        testUser.setName("");
        User savedUser = userStorage.addUser(testUser);

        assertThat(savedUser.getName()).isEqualTo("testuser");
    }
}