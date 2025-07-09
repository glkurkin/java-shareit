package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestServiceMockTest {
    @Mock
    private RequestStorage requestStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private ItemRequestMapper requestMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private ItemStorage itemStorage;
    @InjectMocks
    private ItemRequestServiceImpl requestService;
    private User user;
    private ItemRequest itemRequest;
    private ItemRequest itemRequest2;
    private ItemRequestCreateDto itemRequestCreateDto;
    private ItemRequestWithItemsDto itemRequestWithItemsDto;
    private ItemDto itemDto;
    private Item item;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Name");
        user.setEmail("email@email.ru");

        itemRequest = new ItemRequest();
        itemRequest.setId(1);
        itemRequest.setRequester(user);
        itemRequest.setDescription("Нужна вещь");
        itemRequest.setCreated(LocalDateTime.now().minusDays(2));

        itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("Нужна вещь");

        item = new Item();
        item.setId(1);
        item.setName("Item1");
        item.setDescription("Item1des");
        item.setOwner(user);
        item.setAvailable(true);

        itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
    }

    @Test
    @DisplayName("Создание запроса")
    void create() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(requestMapper.toItemRequest(any(ItemRequestCreateDto.class), any(User.class)))
                .thenAnswer(invocationOnMock -> {
                    ItemRequestCreateDto itemRequestCreateDto = invocationOnMock.getArgument(0);
                    User user = invocationOnMock.getArgument(1);
                    ItemRequest itemRequest = new ItemRequest();
                    itemRequest.setId(1);
                    itemRequest.setRequester(user);
                    itemRequest.setDescription(itemRequestCreateDto.getDescription());
                    itemRequest.setCreated(LocalDateTime.now().minusDays(2));
                    return itemRequest;
                });
        when(requestStorage.save(any(ItemRequest.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        when(requestMapper.toItemRequestDto(any(ItemRequest.class))).thenAnswer(invocationOnMock -> {
            ItemRequest itemRequest = invocationOnMock.getArgument(0);
            ItemRequestDto itemRequestDto = new ItemRequestDto();
            itemRequestDto.setId(itemRequest.getId());
            itemRequestDto.setRequesterId(itemRequest.getRequester().getId());
            itemRequestDto.setDescription(itemRequest.getDescription());
            itemRequestDto.setCreated(itemRequest.getCreated());
            return itemRequestDto;
        });

        ItemRequestDto itemRequestDto = requestService.create(itemRequestCreateDto, 1);
        assertThat(itemRequestDto).isNotNull();
        assertThat(itemRequestDto.getDescription()).isEqualTo(itemRequestCreateDto.getDescription());
        assertThat(itemRequestDto.getRequesterId()).isEqualTo(user.getId());

        verify(userStorage, times(1)).findById(1);
        verify(requestMapper, times(1)).toItemRequest(itemRequestCreateDto, user);
        verify(requestMapper, times(1)).toItemRequestDto(any(ItemRequest.class));
        verify(requestStorage, times(1)).save(any(ItemRequest.class));
    }

    @Test
    @DisplayName("Создание запроса на вещь с возвращением null от маппера")
    void createWithNullFromMapper() {
        Integer requesterId = 1;
        User user = new User();
        user.setId(requesterId);
        user.setName("Name");
        user.setEmail("email@email.ru");
        ItemRequestCreateDto itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("Нужна дрель");
        when(userStorage.findById(requesterId)).thenReturn(Optional.of(user));
        when(requestMapper.toItemRequest(any(ItemRequestCreateDto.class), eq(user))).thenReturn(null);
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> requestService.create(itemRequestCreateDto, requesterId));
        verify(userStorage, times(1)).findById(requesterId);
        verify(requestMapper, times(1)).toItemRequest(any(ItemRequestCreateDto.class), eq(user));
        verifyNoInteractions(requestStorage); // requestStorage не должен быть вызван, так как itemRequest == null
    }

    @Test
    @DisplayName("Получение всех запросов пользователя")
    void getAllUserRequests() {
        when(requestStorage.findByRequesterIdOrderByCreatedDesc(1)).thenReturn(List.of(itemRequest));

        when(requestMapper.toItemRequestDtoList(anyList())).thenAnswer(invocation -> {
            List<ItemRequest> requestList = invocation.getArgument(0);
            return requestList.stream()
                    .map(request -> {
                        ItemRequestDto dto = new ItemRequestDto();
                        dto.setId(request.getId());
                        dto.setDescription(request.getDescription());
                        dto.setCreated(request.getCreated());
                        dto.setRequesterId(request.getRequester().getId());
                        return dto;
                    })
                    .toList();
        });
        List<ItemRequestDto> requestDtos = requestService.getAllUserRequests(1);
        assertThat(requestDtos).hasSize(1);
        assertThat(requestDtos.getFirst().getId()).isEqualTo(itemRequest.getId());
        assertThat(requestDtos.getFirst().getDescription()).isEqualTo(itemRequest.getDescription());

        verify(requestStorage, times(1)).findByRequesterIdOrderByCreatedDesc(1);
        verify(requestMapper, times(1)).toItemRequestDtoList(anyList());
    }

    @Test
    @DisplayName("Получение всех запросов")
    void getAllRequests() {
        itemRequest2 = new ItemRequest();
        itemRequest2.setId(2);
        itemRequest2.setRequester(user);
        itemRequest2.setDescription("Нужна вещь 2");
        itemRequest2.setCreated(LocalDateTime.now().minusDays(5));

        when(requestStorage.findAll()).thenReturn(List.of(itemRequest, itemRequest2));
        when(requestMapper.toItemRequestDtoList(anyList())).thenAnswer(invocationOnMock -> {
            List<ItemRequest> itemRequests = invocationOnMock.getArgument(0);
            return itemRequests.stream().map(request -> {
                ItemRequestDto itemRequestDto = new ItemRequestDto();
                itemRequestDto.setId(request.getId());
                itemRequestDto.setDescription(request.getDescription());
                itemRequestDto.setCreated(request.getCreated());
                itemRequestDto.setRequesterId(request.getRequester().getId());
                return itemRequestDto;
            }).toList();
        });

        List<ItemRequestDto> itemRequestDtos = requestService.getAllRequests();
        assertThat(itemRequestDtos).hasSize(2);
        assertThat(itemRequestDtos.getFirst().getDescription()).isEqualTo("Нужна вещь");
        assertThat(itemRequestDtos.getLast().getDescription()).isEqualTo("Нужна вещь 2");

        verify(requestStorage, times(1)).findAll();
        verify(requestMapper, times(1)).toItemRequestDtoList(anyList());
    }

    @Test
    @DisplayName("Получение запросa по его ИД")
    void getByRequstId() {
        when(requestStorage.findById(1)).thenReturn(Optional.of(itemRequest));
        when(itemStorage.findByRequestIdIn(List.of(1))).thenReturn(List.of(item));
        when(itemMapper.toItemDtoList(anyList())).thenAnswer(invocationOnMock -> {
            List<Item> items = invocationOnMock.getArgument(0);
            return items.stream()
                    .map(item -> {
                        ItemDto itemDto = new ItemDto();
                        itemDto.setId(item.getId());
                        itemDto.setName(item.getName());
                        itemDto.setDescription(item.getDescription());
                        itemDto.setAvailable(item.getAvailable());
                        return itemDto;
                    }).toList();
        });
        when(requestMapper.toItemRequestWithItems(any(ItemRequest.class), anyList())).thenAnswer(invocationOnMock -> {
            ItemRequest itemRequest = invocationOnMock.getArgument(0);
            List<ItemDto> itemDtos = invocationOnMock.getArgument(1);
            ItemRequestWithItemsDto itemRequestWithItemsDto = new ItemRequestWithItemsDto();
            itemRequestWithItemsDto.setId(itemRequest.getId());
            itemRequestWithItemsDto.setDescription(itemRequest.getDescription());
            itemRequestWithItemsDto.setItems(itemDtos);
            itemRequestWithItemsDto.setCreated(itemRequest.getCreated());
            return itemRequestWithItemsDto;
        });

        ItemRequestWithItemsDto itemRequestWithItemsDto = requestService.getByRequstId(1);
        assertThat(itemRequestWithItemsDto).isNotNull();
        assertThat(itemRequestWithItemsDto.getId()).isEqualTo(itemRequest.getId());
        assertThat(itemRequestWithItemsDto.getItems()).hasSize(1);
        assertThat(itemRequestWithItemsDto.getItems().getFirst().getName()).isEqualTo(itemDto.getName());
        assertThat(itemRequestWithItemsDto.getItems().getFirst().getDescription()).isEqualTo(itemDto.getDescription());

        verify(requestStorage, times(1)).findById(1);
        verify(itemStorage, times(1)).findByRequestIdIn(List.of(1));
        verify(itemMapper, times(1)).toItemDtoList(anyList());
        verify(requestMapper, times(1)).toItemRequestWithItems(any(ItemRequest.class), anyList());
    }
}
