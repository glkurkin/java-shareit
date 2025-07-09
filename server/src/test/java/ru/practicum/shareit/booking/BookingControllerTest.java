package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;
    private BookingRequestDto bookingRequestDto;
    private BookingDto bookingDto;
    private ItemDto itemDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(1);
        itemDto.setName("Вещь 1");
        itemDto.setDescription("Описание вещи 1");
        itemDto.setOwner(userDto);
        itemDto.setAvailable(true);
        itemDto.setRequestId(1);

        userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("Иван");
        userDto.setEmail("vanyane@mail.ru");

        bookingDto = new BookingDto();
        bookingDto.setBooker(userDto);
        bookingDto.setStatus(BookingStatus.WAITING);
        bookingDto.setStart(LocalDateTime.now());
        bookingDto.setEnd(LocalDateTime.now().plusHours(5));
        bookingDto.setItem(itemDto);
    }

    @Test
    @DisplayName("Создание бронирования")
    void createBooking() throws Exception {
        when(bookingService.addNewBooking(eq(1), any(BookingRequestDto.class))).thenReturn(bookingDto);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.start").value(bookingDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.end").value(bookingDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()))
                .andExpect(jsonPath("$.item.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.item.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.booker.id").value(userDto.getId()))
                .andExpect(jsonPath("$.booker.name").value(userDto.getName()));

        verify(bookingService, times(1)).addNewBooking(eq(1), any(BookingRequestDto.class));
    }

    @Test
    @DisplayName("Подтверждение бронирования")
    void updateBooking() throws Exception {
        when(bookingService.updateBooking(eq(1), eq(1), eq(true))).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.item.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));

        verify(bookingService, times(1)).updateBooking(eq(1), eq(1), eq(true));
    }

    @Test
    @DisplayName("Получение бронирование пользователя")
    void getBookingByUser() throws Exception {
        when(bookingService.getByUserId(eq(1), eq(1))).thenReturn(bookingDto);
        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.item.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));
        verify(bookingService, times(1)).getByUserId(eq(1), eq(1));
    }

    @Test
    @DisplayName("Получение бронирований владельца вещи")
    void getBookingByOwner() throws Exception {
        when(bookingService.getByOwnerId(eq(1), eq(BookingState.ALL)))
                .thenReturn(Collections.singletonList(bookingDto));
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingDto.getId()))
                .andExpect(jsonPath("$[0].item.id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].status").value(bookingDto.getStatus().toString()));
        verify(bookingService, times(1)).getByOwnerId(eq(1), eq(BookingState.ALL));
    }


}
