package ru.practicum.shareit.items;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundExcep;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mappers.CommentMapper;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentStorage;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.RequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ItemServiceMockTest {
    @Mock
    private ItemStorage itemStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private CommentStorage commentStorage;
    @Mock
    private BookingStorage bookingStorage;
    @Mock
    private RequestStorage requestStorage;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @InjectMocks
    private ItemServiceImpl itemService;
    private Item item;
    private ItemDto itemDto;
    private User user;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Name");
        user.setEmail("email@email.ru");

        item = new Item();
        item.setId(1);
        item.setName("Item1");
        item.setDescription("Item1des");
        item.setOwner(user);
        item.setAvailable(true);

        itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setName("Item1");
        itemDto.setDescription("Item1des");
        itemDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setText("CommentText");
    }

    @Test
    @DisplayName("Добавление нового предмета")
    void addItem() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(itemMapper.toItem(any(ItemDto.class))).thenAnswer(invocation -> {
            ItemDto dto = invocation.getArgument(0);
            Item newItem = new Item();
            newItem.setId(dto.getId());
            newItem.setName(dto.getName());
            newItem.setDescription(dto.getDescription());
            newItem.setAvailable(dto.getAvailable());
            newItem.setOwner(user);
            return newItem;
        });
        when(itemStorage.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemMapper.toItemDto(any(Item.class))).thenAnswer(invocation -> {
            Item savedItem = invocation.getArgument(0);
            ItemDto dto = new ItemDto();
            dto.setId(savedItem.getId());
            dto.setName(savedItem.getName());
            dto.setDescription(savedItem.getDescription());
            dto.setAvailable(savedItem.getAvailable());
            return dto;
        });
        ItemDto resultDto = itemService.addNewItem(1, itemDto);

        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getName()).isEqualTo("Item1");
        assertThat(resultDto.getDescription()).isEqualTo("Item1des");
        verify(userStorage, times(1)).findById(1);
        verify(itemStorage, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("Обновление предмета")
    void updateItem() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(itemStorage.findById(1)).thenReturn(Optional.of(item));
        when(itemStorage.save(any(Item.class))).thenAnswer(invocation -> {
            Item savedItem = invocation.getArgument(0);
            return savedItem;
        });

        doAnswer(invocation -> {
            Item updatedItem = invocation.getArgument(0);
            if (updatedItem == null) {
                return null;
            }
            ItemDto updatedDto = new ItemDto();
            updatedDto.setId(updatedItem.getId());
            updatedDto.setName(updatedItem.getName());
            updatedDto.setDescription(updatedItem.getDescription());
            updatedDto.setAvailable(updatedItem.getAvailable());
            return updatedDto;
        }).when(itemMapper).toItemDto(any(Item.class));
        ItemDto dtoForUpdate = new ItemDto();
        dtoForUpdate.setName("Обновленный предмет");
        dtoForUpdate.setDescription("Новое описание");
        ItemDto dtoFromDb = itemService.updateItem(1, 1, dtoForUpdate);
        assertThat(dtoFromDb).isNotNull();
        assertThat(dtoFromDb.getName()).isEqualTo("Обновленный предмет");
        assertThat(dtoFromDb.getDescription()).isEqualTo("Новое описание");
        verify(userStorage, times(1)).findById(1);
        verify(itemStorage, times(1)).findById(1);
        verify(itemStorage, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("Получение предмета по его ИД")
    void getItemById() {
        when(itemStorage.findById(1)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(any(Item.class))).thenAnswer(invocationOnMock -> {
            Item itemFromDb = invocationOnMock.getArgument(0);
            ItemDto itemDto = new ItemDto();
            itemDto.setId(itemFromDb.getId());
            itemDto.setName(itemFromDb.getName());
            itemDto.setDescription(itemFromDb.getDescription());
            itemDto.setAvailable(itemFromDb.getAvailable());
            return itemDto;
        });
        ItemDto item = itemService.getByItemId(1, 1);
        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("Item1");
        assertThat(item.getDescription()).isEqualTo("Item1des");
        verify(itemStorage, times(1)).findById(1);
    }

    @Test
    @DisplayName("Поиск предметов  по ИД владельца")
    void getItemByOwnerId() {
        List<Item> items = List.of(item);
        when(itemStorage.findAllByOwnerId(1)).thenReturn(items);
        when(itemMapper.toItemDtoList(anyList())).thenAnswer(invocationOnMock -> {
            List<Item> itemList = invocationOnMock.getArgument(0);
            return itemList.stream().map(i -> {
                ItemDto itemDto = new ItemDto();
                itemDto.setId(i.getId());
                itemDto.setName(i.getName());
                itemDto.setDescription(i.getDescription());
                itemDto.setAvailable(i.getAvailable());
                return itemDto;
            }).toList();
        });
        List<ItemDto> itemDtos = itemService.findAllByOwnerId(1);
        assertThat(itemDtos).hasSize(1);
        assertThat(itemDtos.getFirst().getName()).isEqualTo("Item1");
        assertThat(itemDtos.getFirst().getDescription()).isEqualTo("Item1des");
        verify(itemStorage, times(1)).findAllByOwnerId(1);
    }

    @Test
    @DisplayName("Поиск предмета по тексту")
    void searchByText() {
        List<Item> items = List.of(item);
        when(itemStorage.findBySearchText("des")).thenReturn(items);
        when(itemMapper.toItemDtoList(anyList())).thenAnswer(invocationOnMock -> {
            List<Item> itemList = invocationOnMock.getArgument(0);
            return itemList.stream().map(i -> {
                ItemDto itemDto = new ItemDto();
                itemDto.setId(i.getId());
                itemDto.setName(i.getName());
                itemDto.setDescription(i.getDescription());
                itemDto.setAvailable(i.getAvailable());
                return itemDto;
            }).toList();
        });
        List<ItemDto> itemDtos = itemService.searchByText("des");
        assertThat(itemDtos).hasSize(1);
        assertThat(itemDtos.getFirst().getName()).isEqualTo("Item1");
        assertThat(itemDtos.getFirst().getDescription()).isEqualTo("Item1des");
        verify(itemStorage, times(1)).findBySearchText("des");
    }

    @Test
    @DisplayName("Добавление комментария")
    void addComment() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(itemStorage.findById(1)).thenReturn(Optional.of(item));

        when(commentMapper.toComment(any(CommentDto.class))).thenAnswer(invocationOnMock -> {
            CommentDto comm = invocationOnMock.getArgument(0);
            Comment comment = new Comment();
            comment.setId(comm.getId());
            comment.setText(comm.getText());
            comment.setCreated(comm.getCreated());
            return comment;
        });

        when(commentStorage.save((any(Comment.class)))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        List<Booking> approvBookings = List.of(new Booking(
                1,
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().plusDays(2),
                item,
                user,
                BookingStatus.APPROVED
        ));
        when(bookingStorage.findAllApprovedByItemIdAndBookerId(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(approvBookings);

        when(commentMapper.toCommentDto(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            CommentDto dto = new CommentDto();
            dto.setId(comment.getId());
            dto.setText(comment.getText());
            dto.setCreated(comment.getCreated());
            return dto;
        });

        CommentDto comment = itemService.addComment(commentDto, 1, 1);

        assertThat(comment).isNotNull();
        assertThat(comment.getText()).isEqualTo("CommentText");
        verify(userStorage, times(1)).findById(1);
        verify(itemStorage, times(1)).findById(1);
        verify(commentStorage, times(1)).save((any()));
    }

    @Test
    @DisplayName("Добавление комментария к несуществующему предмету")
    void addCommentToInvalidItem() {
        Integer userId = user.getId();
        Integer invalidItemId = 999;

        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(invalidItemId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundExcep.class)
                .isThrownBy(() -> itemService.addComment(commentDto, userId, invalidItemId))
                .withMessageContaining("Предмет не найден");

        verify(userStorage, times(1)).findById(userId);
        verify(itemStorage, times(1)).findById(invalidItemId);
        verify(commentStorage, never()).save(any(Comment.class));
    }

}