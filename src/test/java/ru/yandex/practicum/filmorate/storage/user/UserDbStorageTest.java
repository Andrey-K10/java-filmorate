package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    public void testFindUserById() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.addUser(user);
        Optional<User> userOptional = Optional.of(userStorage.getUserById(createdUser.getId()));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", createdUser.getId())
                );
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User();
        user1.setEmail("test1@mail.ru");
        user1.setLogin("testLogin1");
        user1.setName("Test Name 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("test2@mail.ru");
        user2.setLogin("testLogin2");
        user2.setName("Test Name 2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));

        userStorage.addUser(user1);
        userStorage.addUser(user2);

        List<User> users = userStorage.getAllUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.addUser(user);
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@mail.ru");

        User updatedUser = userStorage.updateUser(createdUser);

        assertThat(updatedUser)
                .hasFieldOrPropertyWithValue("id", createdUser.getId())
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("email", "updated@mail.ru");
    }

    @Test
    public void testAddUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.addUser(user);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive();
        assertThat(createdUser.getName()).isEqualTo("Test Name");
        assertThat(createdUser.getEmail()).isEqualTo("test@mail.ru");
        assertThat(createdUser.getLogin()).isEqualTo("testLogin");
    }
}