package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicatedEmailExcep;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceMockTest {
    @Mock
    UserStorage userStorage;
    @InjectMocks
    private UserServiceImpl userService;
    private User user;
    private User updatedUser;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Name");
        user.setEmail("email@email.ru");

        updatedUser = new User();
        updatedUser.setId(2);
        updatedUser.setName("secondName");
        updatedUser.setEmail("second@email.ru");
    }

    @Test
    @DisplayName("Добавление нового пользователя")
    void addUser() {
        when(userStorage.findAll()).thenReturn(new ArrayList<>());
        User userFromDb = userService.addUser(user);
        assertThat(userFromDb).isNotNull();
        assertThat(userFromDb.getName()).isEqualTo("Name");
        assertThat(userFromDb.getEmail()).isEqualTo("email@email.ru");
        verify(userStorage, times(1)).save(user);
    }

    @Test
    @DisplayName("Добавление пользователя с повторным емейл")
    void addUserWithDuplicatedEmail() {
        List<User> users = new ArrayList<>();
        users.add(user);
        when(userStorage.findAll()).thenReturn(users);
        User duplicatedUser = new User();
        duplicatedUser.setId(2);
        duplicatedUser.setName("NameTwo");
        duplicatedUser.setEmail("email@email.ru");
        assertThatExceptionOfType(DuplicatedEmailExcep.class).isThrownBy(() -> userService.addUser(duplicatedUser));
        verify(userStorage, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(userStorage.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updUser = userService.updUser(1, updatedUser);
        assertThat(updUser).isNotNull();
        assertThat(updUser.getName()).isEqualTo("secondName");
        assertThat(updUser.getEmail()).isEqualTo("second@email.ru");
        verify(userStorage, times(1)).findById(1);
        verify(userStorage, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Поиск пользователя по ИД")
    void findUserById() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        User userFromDb = userService.getUserById(1);
        assertThat(userFromDb).isNotNull();
        assertThat(userFromDb.getName()).isEqualTo("Name");
        assertThat(userFromDb.getEmail()).isEqualTo("email@email.ru");
        verify(userStorage, times(1)).findById(1);
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(updatedUser);
        when(userStorage.findAll()).thenReturn(users);
        List<User> usersFromDb = userService.getAll();
        assertThat(usersFromDb).hasSize(2);
        assertThat(usersFromDb).extracting(User::getName).containsExactlyInAnyOrder("Name", "secondName");
        verify(userStorage, times(1)).findAll();
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser() {
        userService.delete(1);
        verify(userStorage, times(1)).deleteById(1);
    }
}
