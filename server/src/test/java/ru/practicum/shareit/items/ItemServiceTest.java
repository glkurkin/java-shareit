package ru.practicum.shareit.items;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestExcep;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemServiceTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemStorage itemStorage;
    @Autowired
    private BookingStorage bookingStorage;
    @Autowired
    private BookingService bookingService;
    private Booking booking;
    private User user1;
    private User owner;
    private ItemDto item1;
    private CommentDto commentDto;


    @BeforeEach
    void setUp() {
        item1 = new ItemDto();

        user1 = new User();
        user1.setName("Иван");
        user1.setEmail("vanya@mail.ru");

        owner = new User();
        owner.setName("Владелец");
        owner.setEmail("owner@owner.ru");
        userService.addUser(owner);


        item1.setName("Перфоратор");
        item1.setDescription("Для отверстий в стене");
        item1.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setText("Хорошая штука!");
    }

    @Test
    @DisplayName("Добавление предмета")
    void addItem() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        assertThat(newItem).isNotNull();
        assertThat(newItem.getName()).isEqualTo("Перфоратор");
        assertThat(newItem.getDescription()).isEqualTo("Для отверстий в стене");
        assertThat(newItem.getAvailable()).isEqualTo(true);
    }

    @Test
    @DisplayName("Добавление предмета несуществующим пользователем")
    void addItemWithInvalidUser() {
        int invalidUserId = 999;
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> itemService.addNewItem(invalidUserId, item1))
                .withMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Добавление предмета с несуществующим запросом")
    void addItemWithInvalidRequest() {
        // Arrange: создаем DTO для предмета с несуществующим requestId
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item1");
        itemDto.setDescription("Item1des");
        itemDto.setAvailable(true);
        itemDto.setRequestId(999); // Несуществующий requestId

        // Act & Assert: проверяем, что выбрасывается исключение
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> itemService.addNewItem(owner.getId(), itemDto))
                .withMessageContaining("Запрос на вещь не найден");
    }

    @Test
    @DisplayName("Обновление предмета")
    void updateItem() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        ItemDto updatedDto = new ItemDto();
        updatedDto.setName("Мясорубка");
        updatedDto.setDescription("Для прокрутки мяса");
        ItemDto myasorubka = itemService.updateItem(owner.getId(), newItem.getId(), updatedDto);
        assertThat(myasorubka.getName()).isEqualTo("Мясорубка");
        assertThat(myasorubka.getDescription()).isEqualTo("Для прокрутки мяса");
    }

    @Test
    @DisplayName("Обновление несуществующего предмета")
    void updateNonExistingItem() {
        int invalidItemId = 999;
        ItemDto updatedDto = new ItemDto();
        updatedDto.setName("Мясорубка");
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> itemService.updateItem(owner.getId(), invalidItemId, updatedDto))
                .withMessageContaining("Предмет не найден");
    }

    @Test
    @DisplayName("Обновление только имени предмета")
    void updateOnlyName() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        ItemDto updatedDto = new ItemDto();
        updatedDto.setName("Новое имя");
        ItemDto updatedItem = itemService.updateItem(owner.getId(), newItem.getId(), updatedDto);
        assertThat(updatedItem.getName()).isEqualTo("Новое имя");
        assertThat(updatedItem.getDescription()).isEqualTo(item1.getDescription());
        assertThat(updatedItem.getAvailable()).isEqualTo(item1.getAvailable());
    }

    @Test
    @DisplayName("Обновление только описания предмета")
    void updateOnlyDescription() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        ItemDto updatedDto = new ItemDto();
        updatedDto.setDescription("Новое описание");
        ItemDto updatedItem = itemService.updateItem(owner.getId(), newItem.getId(), updatedDto);
        assertThat(updatedItem.getName()).isEqualTo(item1.getName());
        assertThat(updatedItem.getDescription()).isEqualTo("Новое описание");
        assertThat(updatedItem.getAvailable()).isEqualTo(item1.getAvailable());
    }

    @Test
    @DisplayName("Обновление предмета не существующим пользователем")
    void updateItemByAnotherUser() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        User anotherUser = new User();
        anotherUser.setId(3);
        anotherUser.setName("Другой пользователь");
        anotherUser.setEmail("user3@ya.com");
        userService.addUser(anotherUser);
        ItemDto updatedDto = new ItemDto();
        updatedDto.setName("Мясорубка");
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> itemService.updateItem(anotherUser.getId(), newItem.getId(), updatedDto))
                .withMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Обновление с пустыми или нулевыми значениями")
    void updateWithEmptyOrNullValues() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        ItemDto updatedDto = new ItemDto();
        updatedDto.setName("");
        updatedDto.setDescription(null);
        ItemDto updatedItem = itemService.updateItem(owner.getId(), newItem.getId(), updatedDto);
        assertThat(updatedItem.getName()).isEqualTo(item1.getName());
        assertThat(updatedItem.getDescription()).isEqualTo(item1.getDescription());
        assertThat(updatedItem.getAvailable()).isEqualTo(item1.getAvailable());
    }

    @Test
    @DisplayName("Обновление всеми полями")
    void updateAllFields() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        ItemDto updatedDto = new ItemDto();
        updatedDto.setName("Новое имя");
        updatedDto.setDescription("Новое описание");
        updatedDto.setAvailable(false);
        ItemDto updatedItem = itemService.updateItem(owner.getId(), newItem.getId(), updatedDto);
        assertThat(updatedItem.getName()).isEqualTo("Новое имя");
        assertThat(updatedItem.getDescription()).isEqualTo("Новое описание");
        assertThat(updatedItem.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("Поиск предмета по ИД")
    void getItemById() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        ItemDto itemFromDb = itemService.getByItemId(owner.getId(), newItem.getId());
        assertThat(itemFromDb.getName()).isEqualTo("Перфоратор");
        assertThat(itemFromDb.getDescription()).isEqualTo("Для отверстий в стене");
    }

    @Test
    @DisplayName("Поиск по ИД владельца предметов")
    void getItemsByOwnerId() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        ItemDto item2 = new ItemDto();
        item2.setName("Мясорубка");
        item2.setDescription("Для прокрутки мяса");
        itemService.addNewItem(owner.getId(), item2);
        List<ItemDto> allByOwnerId = itemService.findAllByOwnerId(owner.getId());
        assertThat(allByOwnerId).hasSize(2);
        assertThat(allByOwnerId).extracting(ItemDto::getName).containsExactlyInAnyOrder("Перфоратор", "Мясорубка");
    }

    @Test
    @DisplayName("Поиск предмета по тексту")
    void searchItemByText() {
        ItemDto newItem = itemService.addNewItem(owner.getId(), item1);
        ItemDto item2 = new ItemDto();
        item2.setName("Вилка");
        item2.setDescription("Для отверстий в мясе");
        item2.setAvailable(true);
        itemService.addNewItem(owner.getId(), item2);
        List<ItemDto> founded = itemService.searchByText("отверстий");
        System.out.println(founded.toString());
        assertThat(founded).hasSize(2);
        assertThat(founded).extracting(ItemDto::getName).containsExactlyInAnyOrder("Перфоратор", "Вилка");
    }

    @Test
    @DisplayName("Поиск предмета по пустому тексту")
    void searchItemByEmptyText() {
        itemService.addNewItem(owner.getId(), item1);
        List<ItemDto> founded = itemService.searchByText("");
        assertThat(founded).isEmpty();
    }


    @Test
    @DisplayName("Добавление комментария")
    void addCommentForItem() {
        userService.addUser(user1);
        Item item = new Item();
        item.setName("Вещь для ДБ");
        item.setDescription("Описание вещи для ДБ");
        item.setAvailable(true);
        item.setOwner(owner);
        itemStorage.save(item);
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setItem(item);
        booking.setBooker(user1);
        booking.setStatus(BookingStatus.APPROVED);
        bookingStorage.save(booking);
        itemService.addComment(commentDto, user1.getId(), item.getId());
        ItemDto itemFromDb = itemService.getByItemId(user1.getId(), item.getId());
        assertThat(itemFromDb.getComments().getFirst()).isNotNull();
        assertThat(itemFromDb.getComments().getFirst().getText()).isEqualTo("Хорошая штука!");
    }

    @Test
    @DisplayName("Добавление комментария к предмету которого нет")
    void addCommentToNullItem() {
        userService.addUser(user1);
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> itemService.addComment(commentDto, user1.getId(), 70))
                .withMessage("Предмет не найден");
    }

    @Test
    @DisplayName("Добавление комментария от пользователя которого нет")
    void addCommentWithNullUser() {
        userService.addUser(user1);
        itemService.addNewItem(user1.getId(), item1);
        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> itemService.addComment(commentDto, 23, item1.getId()))
                .withMessage("Пользователь не найден");
    }

    @Test
    @DisplayName("Добавление комментария без бронирования")
    void addCommentWithoutBooking() {
        userService.addUser(user1);
        Item item = new Item();
        item.setName("Вещь для ДБ");
        item.setDescription("Описание вещи для ДБ");
        item.setAvailable(true);
        item.setOwner(owner);
        itemStorage.save(item);
        assertThatExceptionOfType(BadRequestExcep.class).isThrownBy(() -> itemService.addComment(commentDto, user1.getId(), item.getId()))
                .withMessageContaining("которые было бронирование");
    }

    @Test
    @DisplayName("Получение предмета с комментариями")
    void getItemWithComments() {
        userService.addUser(user1);
        Item item = new Item();
        item.setName("Вещь для ДБ");
        item.setDescription("Описание вещи для ДБ");
        item.setAvailable(true);
        item.setOwner(owner);
        itemStorage.save(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setItem(item);
        booking.setBooker(user1);
        booking.setStatus(BookingStatus.APPROVED);
        bookingStorage.save(booking);

        CommentDto comment = new CommentDto();
        comment.setText("Отличная вещь!");
        itemService.addComment(comment, user1.getId(), item.getId());

        ItemDto itemDto = itemService.getByItemId(owner.getId(), item.getId());

        assertThat(itemDto.getComments()).hasSize(1);
        assertThat(itemDto.getComments().getFirst().getText()).isEqualTo("Отличная вещь!");
    }
}
