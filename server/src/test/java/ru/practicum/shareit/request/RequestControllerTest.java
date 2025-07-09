package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class RequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    ItemRequestService itemRequestService;
    private ItemRequestCreateDto itemRequestCreateDto;
    private ItemRequestDto itemRequestDto;
    private ItemRequestDto itemRequestDto2;
    private ItemRequest request;
    private ItemDto itemDto;
    private CommentDto commentDto;
    private UserDto userDto;
    private ItemRequestWithItemsDto itemRequestWithItemsDto;

    @BeforeEach
    void setUp() {
        itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("Нужна вещь");
        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setRequesterId(1);
        itemRequestDto.setId(1);
        itemRequestDto.setCreated(LocalDateTime.now());
        itemRequestDto.setDescription("Описание вещи");

        itemRequestDto2 = new ItemRequestDto();
        itemRequestDto2.setRequesterId(1);
        itemRequestDto2.setId(1);
        itemRequestDto2.setCreated(LocalDateTime.now());
        itemRequestDto2.setDescription("Описание второй вещи");

        userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("Иван");
        userDto.setEmail("vanyane@mail.ru");

        commentDto = new CommentDto();
        commentDto.setId(1);
        commentDto.setItemId(1);
        commentDto.setText("Классная штука!");
        commentDto.setCreated(LocalDateTime.now().minusHours(2));
        commentDto.setAuthorName("Петр");

        itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setName("Вещь 1");
        itemDto.setDescription("Описание вещи 1");
        itemDto.setOwner(userDto);
        itemDto.setAvailable(true);
        itemDto.setRequestId(1);
        itemDto.setComments(Collections.singletonList(commentDto));

        itemRequestDto.setItems(Collections.singletonList(itemDto));

        itemRequestWithItemsDto = new ItemRequestWithItemsDto();
        itemRequestWithItemsDto.setId(1);
        itemRequestWithItemsDto.setDescription("Описание");
        itemRequestWithItemsDto.setItems(Collections.singletonList(itemDto));
        itemRequestWithItemsDto.setCreated(LocalDateTime.now());
    }

    @Test
    @DisplayName("Создание запроса")
    void addRequest() throws Exception {
        when(itemRequestService.create(any(ItemRequestCreateDto.class), eq(1))).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.requesterId").value(itemRequestDto.getRequesterId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()));

        verify(itemRequestService, times(1)).create(any(ItemRequestCreateDto.class), eq(1));
    }

    @Test
    @DisplayName("Получение всех запросов")
    void getAllRequests() throws Exception {
        when(itemRequestService.getAllRequests()).thenReturn(List.of(itemRequestDto));
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$[0].requesterId").value(itemRequestDto.getRequesterId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDto.getDescription()));

        verify(itemRequestService, times(1)).getAllRequests();
    }

    @Test
    @DisplayName("Получение всех запросов пользователя")
    void getAllUserRequests() throws Exception {
        when(itemRequestService.getAllUserRequests(eq(1))).thenReturn(List.of(itemRequestDto, itemRequestDto2));
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].id").value(itemRequestDto2.getId()))
                .andExpect(jsonPath("$[1].requesterId").value(itemRequestDto2.getRequesterId()))
                .andExpect(jsonPath("$[1].description").value(itemRequestDto2.getDescription()));
        verify(itemRequestService, times(1)).getAllUserRequests(eq(1));
    }

    @Test
    @DisplayName("Получение всех вещи по ИД запроса")
    void getItemRequestById() throws Exception {
        when(itemRequestService.getByRequstId(eq(1))).thenReturn(itemRequestWithItemsDto);
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestWithItemsDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestWithItemsDto.getDescription()))
                .andExpect(jsonPath("$.items.[0].name").value(itemRequestWithItemsDto.getItems().getFirst().getName()))
                .andExpect(jsonPath("$.items.[0].description").value(itemRequestDto.getItems().getFirst().getDescription()));
        verify(itemRequestService, times(1)).getByRequstId(1);
    }

}
