package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private static Validator validator;
    private static UserController userController;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        userController = new UserController();
    }

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации для корректного пользователя");
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        User user = new User();
        user.setEmail("   ");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Электронная почта должна содержать символ @", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("   ");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenLoginIsNull() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(null);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test user");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Логин не может содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    void shouldAcceptLoginWithoutSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Должен принимать логин без пробелов");
    }

    @Test
    void shouldAcceptNullName() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Должен принимать null имя");
    }

    @Test
    void shouldAcceptBlankName() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("   ");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Должен принимать пустое имя");
    }

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    void shouldAcceptBirthdayToday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Должен принимать сегодняшнюю дату");
    }

    @Test
    void shouldAcceptBirthdayInPast() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Должен принимать дату в прошлом");
    }

    @Test
    void shouldHandleEmptyNameInController() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getName(), "Должен использовать логин, если имя пустое");
    }

    @Test
    void shouldHandleNullNameInController() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getName(), "Должен использовать логин, если имя null");
    }

    @Test
    void shouldHandleMultipleValidationErrors() {
        User user = new User();
        user.setEmail("invalid");
        user.setLogin("   ");
        user.setName("Test User");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(3, violations.size());
    }
}