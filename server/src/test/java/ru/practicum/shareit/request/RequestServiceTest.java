package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RequestServiceTest {
    private User user1;
    private ItemRequestCreateDto itemRequestCreateDto;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private ItemRequestService itemRequestService;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("Иван");
        user1.setEmail("vanya@mail.ru");
        userStorage.save(user1);

        itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("Нужен перфоратор");
    }

    @Test
    @DisplayName("Создание запроса")
    void createRequest() {
        ItemRequestDto itemRequestDto = itemRequestService.create(itemRequestCreateDto, user1.getId());
        assertThat(itemRequestDto).isNotNull();
        assertThat(itemRequestDto.getDescription()).isEqualTo(itemRequestCreateDto.getDescription());
    }

    @Test
    @DisplayName("Получение всех запросов пользователя")
    void getAllUserRequests() {
        ItemRequestDto itemRequestDto = itemRequestService.create(itemRequestCreateDto, user1.getId());
        ItemRequestCreateDto itemRequestCreateDto1 = new ItemRequestCreateDto();
        itemRequestCreateDto1.setDescription("Нужна еще какая-то вещь");
        ItemRequestDto itemRequestDto1 = itemRequestService.create(itemRequestCreateDto1, user1.getId());
        List<ItemRequestDto> requestDtos = itemRequestService.getAllUserRequests(user1.getId());
        assertThat(requestDtos).isNotNull();
        assertThat(requestDtos.size()).isEqualTo(2);
        assertThat(requestDtos.getLast().getDescription()).isEqualTo("Нужен перфоратор");
    }

    @Test
    @DisplayName("Получение всех запросов")
    void getAllRequests() {
        ItemRequestDto itemRequestDto = itemRequestService.create(itemRequestCreateDto, user1.getId());
        ItemRequestCreateDto itemRequestCreateDto1 = new ItemRequestCreateDto();
        itemRequestCreateDto1.setDescription("Нужна еще какая-то вещь");
        ItemRequestDto itemRequestDto1 = itemRequestService.create(itemRequestCreateDto1, user1.getId());
        List<ItemRequestDto> requestDtos = itemRequestService.getAllRequests();
        assertThat(requestDtos).isNotNull();
        assertThat(requestDtos.size()).isEqualTo(2);
        assertThat(requestDtos.getLast().getDescription()).isEqualTo("Нужна еще какая-то вещь");
    }

    @Test
    @DisplayName("Получение запроса по его ИД")
    void getRequestById() {
        ItemRequestDto itemRequestDto = itemRequestService.create(itemRequestCreateDto, user1.getId());
        ItemRequestWithItemsDto requestDtoFromDb = itemRequestService.getByRequstId(itemRequestDto.getId());
        assertThat(requestDtoFromDb).isNotNull();
        assertThat(requestDtoFromDb.getDescription()).isEqualTo("Нужен перфоратор");
        assertThat(requestDtoFromDb.getId()).isEqualTo(itemRequestDto.getId());
    }

    @Test
    @DisplayName("Получение запроса по несуществующему ID")
    void getRequestByInvalidId() {
        int invalidRequestId = 999;
        assertThatThrownBy(() -> itemRequestService.getByRequstId(invalidRequestId))
                .isInstanceOf(NotFoundExcep.class)
                .hasMessageContaining("Запрос не найден");
    }

    @Test
    @DisplayName("Получение запроса без связанных предметов")
    void getRequestWithoutItems() {
        ItemRequestDto itemRequestDto = itemRequestService.create(itemRequestCreateDto, user1.getId());
        ItemRequestWithItemsDto requestWithItems = itemRequestService.getByRequstId(itemRequestDto.getId());
        assertThat(requestWithItems).isNotNull();
        assertThat(requestWithItems.getDescription()).isEqualTo("Нужен перфоратор");
        assertThat(requestWithItems.getItems()).isEmpty();
    }

}
