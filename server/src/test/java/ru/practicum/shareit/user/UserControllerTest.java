package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    private User user;
    private User user2;
    private UserDto userDto;
    private UserDto userDto2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Иван");
        user.setEmail("vanyane@mail.ru");

        userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("Иван");
        userDto.setEmail("vanyane@mail.ru");

        user2 = new User();
        user2.setId(2);
        user2.setName("Петр");
        user2.setEmail("petya@ya.ru");

        userDto2 = new UserDto();
        userDto2.setId(2);
        userDto2.setName("Петр");
        userDto2.setEmail("petya@ya.ru");


    }

    @Test
    @DisplayName("Создание пользователя")
    void createUser() throws Exception {
        when(userService.addUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
        verify(userService, times(1)).addUser(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser() throws Exception {
        when(userService.updUser(eq(1), any(User.class))).thenReturn(user);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
        verify(userService, times(1)).updUser(eq(1), any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя с частичными данными (только имя)")
    void updateUserWithPartialData() throws Exception {
        User updatedUser = new User();
        updatedUser.setName("Иван Обновленный");

        User updatedDbUser = new User();
        updatedDbUser.setId(1);
        updatedDbUser.setName("Иван Обновленный");
        updatedDbUser.setEmail("vanyane@mail.ru");

        when(userService.updUser(eq(1), any(User.class))).thenReturn(updatedDbUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иван Обновленный"))
                .andExpect(jsonPath("$.email").value("vanyane@mail.ru"));
        verify(userService, times(1)).updUser(eq(1), any(User.class));
    }


    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers() throws Exception {
        when(userService.getAll()).thenReturn(List.of(user, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].id").value(userDto2.getId()))
                .andExpect(jsonPath("$[1].name").value(userDto2.getName()))
                .andExpect(jsonPath("$[1].email").value(userDto2.getEmail()));
        verify(userService, times(1)).getAll();
    }

    @Test
    @DisplayName("Получение пользователя по ИД")
    void getById() throws Exception {
        when(userService.getUserById(2)).thenReturn(user2);

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto2.getId()))
                .andExpect(jsonPath("$.name").value(userDto2.getName()))
                .andExpect(jsonPath("$.email").value(userDto2.getEmail()));
        verify(userService, times(1)).getUserById(2);
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему ИД")
    void getUserByInvalidId() throws Exception {
        int invalidUserId = 999;
        when(userService.getUserById(invalidUserId)).thenThrow(new NotFoundExcep("Пользователь не найден"));

        mockMvc.perform(get("/users/{id}", invalidUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
        verify(userService, times(1)).getUserById(invalidUserId);
    }

    @Test
    @DisplayName("Удаление пользователя по ИД")
    void deleteById() throws Exception {
        mockMvc.perform(delete("/users/2")).andExpect(status().isOk());
        verify(userService, times(1)).delete(2);
    }
}
