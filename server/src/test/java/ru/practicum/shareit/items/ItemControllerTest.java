package ru.practicum.shareit.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;
    @Autowired
    private ObjectMapper objectMapper;
    private Item item;
    private User user;
    private UserDto userDto;
    private User user2;
    private ItemRequest itemRequest;
    private ItemDto itemDto;
    private Comment comment;
    private CommentDto commentDto;

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

        itemRequest = new ItemRequest();
        itemRequest.setId(1);
        itemRequest.setDescription("Нужна вещь 1");
        itemRequest.setRequester(user2);

        comment = new Comment();
        comment.setId(1);
        comment.setAuthor(user2);
        comment.setText("Классная штука!");
        comment.setCreated(LocalDateTime.now().minusHours(2));


        commentDto = new CommentDto();
        commentDto.setId(1);
        commentDto.setItemId(1);
        commentDto.setText("Классная штука!");
        commentDto.setCreated(LocalDateTime.now().minusHours(2));
        commentDto.setAuthorName("Петр");

        item = new Item();
        item.setId(1);
        item.setName("Вещь 1");
        item.setDescription("Описание вещи 1");
        item.setOwner(user);
        item.setAvailable(true);
        item.setItemRequest(itemRequest);

        itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setName("Вещь 1");
        itemDto.setDescription("Описание вещи 1");
        itemDto.setOwner(userDto);
        itemDto.setAvailable(true);
        itemDto.setRequestId(1);
        itemDto.setComments(Collections.singletonList(commentDto));
    }

    @Test
    @DisplayName("Создание предмета")
    void createItem() throws Exception {
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(itemDto));
        when(itemService.addNewItem(eq(1), any(ItemDto.class))).thenReturn(itemDto);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.owner.id").value(user.getId()))
                .andExpect(jsonPath("$.owner.name").value(user.getName()))
                .andExpect(jsonPath("$.owner.email").value(user.getEmail()))
                .andExpect(jsonPath("$.requestId").value(itemRequest.getId()))
                .andExpect(jsonPath("$.comments[0].text").value(comment.getText()));
        verify(itemService, times(1)).addNewItem(eq(1), any(ItemDto.class));
    }

    @Test
    @DisplayName("Обновление предмета")
    void updateItem() throws Exception {
        when(itemService.updateItem(eq(1), eq(1), any(ItemDto.class))).thenReturn(itemDto);
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.owner.id").value(user.getId()))
                .andExpect(jsonPath("$.owner.name").value(user.getName()))
                .andExpect(jsonPath("$.owner.email").value(user.getEmail()))
                .andExpect(jsonPath("$.requestId").value(itemRequest.getId()))
                .andExpect(jsonPath("$.comments[0].text").value(comment.getText()));
        verify(itemService, times(1)).updateItem(eq(1), eq(1), any(ItemDto.class));
    }

    @Test
    @DisplayName("Получение предмета по его ИД")
    void getItemById() throws Exception {
        when(itemService.getByItemId(eq(1), eq(1))).thenReturn(itemDto);
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.owner.id").value(user.getId()))
                .andExpect(jsonPath("$.owner.name").value(user.getName()))
                .andExpect(jsonPath("$.owner.email").value(user.getEmail()))
                .andExpect(jsonPath("$.requestId").value(itemRequest.getId()))
                .andExpect(jsonPath("$.comments[0].text").value(comment.getText()));
        verify(itemService, times(1)).getByItemId(eq(1), eq(1));
    }

    @Test
    @DisplayName("Получение предметов пользователя по его ИД")
    void getItemsByUserId() throws Exception {
        when(itemService.findAllByOwnerId(eq(1))).thenReturn(Collections.singletonList(itemDto));
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$[0].owner.id").value(user.getId()))
                .andExpect(jsonPath("$[0].owner.name").value(user.getName()))
                .andExpect(jsonPath("$[0].owner.email").value(user.getEmail()))
                .andExpect(jsonPath("$[0].requestId").value(itemRequest.getId()))
                .andExpect(jsonPath("$[0].comments[0].text").value(comment.getText()));
        verify(itemService, times(1)).findAllByOwnerId(eq(1));
    }

    @Test
    @DisplayName("Поиск предмета по тексту")
    void findItemByText() throws Exception {
        when(itemService.searchByText(eq("вещи"))).thenReturn(Collections.singletonList(itemDto));
        mockMvc.perform(get("/items/search")
                        .param("text", "вещи"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$[0].owner.id").value(user.getId()))
                .andExpect(jsonPath("$[0].owner.name").value(user.getName()))
                .andExpect(jsonPath("$[0].owner.email").value(user.getEmail()))
                .andExpect(jsonPath("$[0].requestId").value(itemRequest.getId()))
                .andExpect(jsonPath("$[0].comments[0].text").value(comment.getText()));
        verify(itemService, times(1)).searchByText(eq("вещи"));
    }

    @Test
    @DisplayName("Добавление комментария к вещи")
    void addComment() throws Exception {
        when(itemService.addComment(any(CommentDto.class), eq(1), eq(1))).thenReturn(commentDto);
        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()));
        verify(itemService, times(1)).addComment(any(CommentDto.class), eq(1), eq(1));

    }
}

