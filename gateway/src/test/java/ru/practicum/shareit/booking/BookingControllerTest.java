package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(
        classes = ru.practicum.shareit.ShareItGateway.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto sampleDto() {
        return BookingDto.builder()
                .id(1L)
                .item(new BookingItemDto(2L, "Drill"))
                .booker(new BookingBookerDto(3L))
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBooking_ReturnsDto() throws Exception {
        BookingDto dto = sampleDto();
        when(bookingService.create(eq(3L), any(BookingRequestDto.class))).thenReturn(dto);

        BookingRequestDto req = new BookingRequestDto(2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .content(mapper.writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.item.id").value(2))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveBooking_ReturnsDto() throws Exception {
        BookingDto dto = sampleDto();
        when(bookingService.approveBooking(1L, 5L, true)).thenReturn(dto);

        mvc.perform(patch("/bookings/5")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()));
    }

    @Test
    void getBookingById_ReturnsDto() throws Exception {
        BookingDto dto = sampleDto();
        when(bookingService.getById(3L, 1L)).thenReturn(dto);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booker.id").value(3));
    }

    @Test
    void getAllByBooker_ReturnsList() throws Exception {
        BookingDto dto = sampleDto();
        when(bookingService.getAllOwnByBooker(3L, "ALL", 0, 10))
                .thenReturn(List.of(dto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId()));
    }

    @Test
    void getAllByOwner_ReturnsList() throws Exception {
        BookingDto dto = sampleDto();
        when(bookingService.getAllOwnByOwner(1L, "ALL", 0, 10))
                .thenReturn(List.of(dto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].item.name").value("Drill"));
    }
}