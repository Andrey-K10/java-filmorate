package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = new User();
        validUser.setEmail("test@mail.ru");
        validUser.setLogin("testlogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void createUser_WithValidData_ShouldSuccess() {
        User result = userController.createUser(validUser);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test@mail.ru", result.getEmail());
    }

    @Test
    void createUser_WithEmptyEmail_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_WithEmailWithoutAt_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_WithEmptyLogin_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_WithLoginContainingSpaces_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login with spaces");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_WithEmptyName_ShouldUseLoginAsName() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User result = userController.createUser(user);

        assertNotNull(result);
        assertEquals("testlogin", result.getName()); // Должен использовать логин как имя
    }

    @Test
    void createUser_WithNullName_ShouldUseLoginAsName() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User result = userController.createUser(user);

        assertNotNull(result);
        assertEquals("testlogin", result.getName()); // Должен использовать логин как имя
    }

    @Test
    void createUser_WithFutureBirthday_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now().plusDays(1)); // Завтра

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_WithTodayBirthday_ShouldSuccess() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now()); // Сегодня

        User result = userController.createUser(user);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void createUser_WithNullBirthday_ShouldSuccess() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(null); // null дата

        User result = userController.createUser(user);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }
}