package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String USERID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto addBooking(@RequestHeader(USERID_HEADER) Integer userId, @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("Бронирование вещи c ID = {} добавлено", bookingRequestDto.getItemId());
        return bookingService.addNewBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto bookingApprove(@RequestHeader(USERID_HEADER) Integer userId, @PathVariable Integer bookingId, @RequestParam boolean approved) {
        log.info("Бронирование c ID = {} обновлено", bookingId);
        return bookingService.updateBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingByUserId(@RequestHeader(USERID_HEADER) Integer userId, @PathVariable Integer bookingId) {
        log.info("Выводим бронирование {} пользователем {}", bookingId, userId);
        return bookingService.getByUserId(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllBookings() {
        log.info("Вывводим все бронирования");
        return bookingService.findAll().reversed();
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader(USERID_HEADER) Integer userId,
                                             @RequestParam(required = false, defaultValue = "ALL") BookingState bookingState) {
        return bookingService.getByOwnerId(userId, bookingState);
    }
}
