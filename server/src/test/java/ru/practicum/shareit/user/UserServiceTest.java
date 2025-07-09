package ru.practicum.shareit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedEmailExcep;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {
    @Autowired
    private UserService userService;

    private User user1;
    private User user2;
    private User incorrectUser;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("Иван");
        user1.setEmail("vanya@mail.ru");

        user2 = new User();
        user2.setName("Петя");
        user2.setEmail("petr@mail.ru");

        incorrectUser = new User();
    }

    @AfterEach
    void clear() {
        userService.getAll().forEach(user -> userService.delete(user.getId()));
    }

    @Test
    @DisplayName("Добавление пользователя в БД")
    void addUser() {
        User userInDb = userService.addUser(user1);
        assertThat(userInDb.getId()).isNotNull();
        assertThat(userInDb.getName()).isEqualTo("Иван");
        assertThat(userInDb.getEmail()).isEqualTo("vanya@mail.ru");

        User savedInDbUser = userService.getUserById(userInDb.getId());
        assertThat(savedInDbUser.getId()).isNotNull();
        assertThat(savedInDbUser.getName()).isEqualTo("Иван");
        assertThat(savedInDbUser.getEmail()).isEqualTo("vanya@mail.ru");
    }


    @Test
    @DisplayName("Добавление пользователя в БД с повторной почтой")
    void addUserWithIncorrectEmaile() {
        userService.addUser(user1);
        incorrectUser.setEmail(user1.getEmail());
        assertThatExceptionOfType(DuplicatedEmailExcep.class)
                .isThrownBy(() -> userService.addUser(incorrectUser))
                .withMessage("Данная почта уже используется");

    }

    @Test
    @DisplayName("Обновление данных пользователя")
    void updUserData() {
        User user3 = new User();
        user3.setName("Кирилл");
        user3.setEmail("user3@gmail.com");
        userService.addUser(user3);
        userService.updUser(user3.getId(), user2);
        User updUser = userService.getUserById(user3.getId());
        assertThat(updUser.getName()).isEqualTo("Петя");
        assertThat(updUser.getEmail()).isEqualTo("petr@mail.ru");
    }

    @Test
    @DisplayName("Обновление пользователя с повторной почтой")
    void updUserWithExistingEmail() {
        User user3 = new User();
        user3.setName("Кирилл");
        user3.setEmail("petr@mail.ru");
        userService.addUser(user3);
        assertThatExceptionOfType(DuplicatedEmailExcep.class)
                .isThrownBy(() -> userService.updUser(user3.getId(), user2))
                .withMessageContaining("почта уже используется");
    }

    @Test
    @DisplayName("Получение пользователя по ИД")
    void getUserById() {
        User userOne = userService.addUser(user1);
        User userTwo = userService.addUser(user2);
        User userFromDb1 = userService.getUserById(userOne.getId());
        User userFromDb2 = userService.getUserById(userTwo.getId());
        assertThat(userFromDb1.getName()).isEqualTo("Иван");
        assertThat(userFromDb2.getName()).isEqualTo("Петя");
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему ИД")
    void getUserByIncorrectId() {
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> userService.getUserById(70))
                .withMessage("Пользователь с ID = 70 не найден");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsersFromDb() {
        userService.addUser(user1);
        userService.addUser(user2);
        List<User> usersFromDb = userService.getAll();
        assertThat(usersFromDb.size()).isEqualTo(2);
        assertThat(usersFromDb).extracting(User::getName).containsExactlyInAnyOrder("Иван", "Петя");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUserFromDb() {
        User userToDb = userService.addUser(user1);
        userService.delete(userToDb.getId());
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> userService.getUserById(userToDb.getId()))
                .withMessage("Пользователь с ID = " + userToDb.getId() + " не найден");
    }
}
