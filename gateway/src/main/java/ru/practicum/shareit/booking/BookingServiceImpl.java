package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingClient client;
    private final ObjectMapper mapper;

    public BookingServiceImpl(BookingClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public BookingDto create(Long userId, BookingRequestDto dto) {
        ResponseEntity<Object> resp = client.bookItem(userId, dto);
        return extractObject(resp, BookingDto.class);
    }

    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        ResponseEntity<Object> resp = client.approveBooking(userId, bookingId, approved);
        return extractObject(resp, BookingDto.class);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        ResponseEntity<Object> resp = client.getBooking(userId, bookingId);
        return extractObject(resp, BookingDto.class);
    }

    @Override
    public List<BookingDto> getAllOwnByBooker(Long bookerId,
                                              String state,
                                              int from,
                                              int size) {
        BookingState st = BookingState.from(state)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Unknown state: " + state));
        ResponseEntity<Object> resp = client.getBookings(bookerId, st, from, size);
        return extractList(resp, new TypeReference<List<BookingDto>>() {
        });
    }

    @Override
    public List<BookingDto> getAllOwnByOwner(Long ownerId,
                                             String state,
                                             int from,
                                             int size) {
        BookingState st = BookingState.from(state)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Unknown state: " + state));
        ResponseEntity<Object> resp = client.getOwnerBookings(ownerId, st, from, size);
        return extractList(resp, new TypeReference<List<BookingDto>>() {
        });
    }

    private <T> T extractObject(ResponseEntity<Object> resp, Class<T> cls) {
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(resp.getStatusCode(), "Error from server");
        }
        return mapper.convertValue(resp.getBody(), cls);
    }

    private <T> T extractList(ResponseEntity<Object> resp, TypeReference<T> typeRef) {
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(resp.getStatusCode(), "Error from server");
        }
        return mapper.convertValue(resp.getBody(), typeRef);
    }
}
