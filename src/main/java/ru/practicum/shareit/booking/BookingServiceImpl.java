package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.ForbiddenException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto create(Long userId, BookingRequestDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + dto.getItemId() + " не найдена"));

        if (item.getOwnerId().equals(userId)) {
            throw new NotFoundException("Нельзя забронировать свою вещь");
        }

        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new BadRequestException("start и end должны быть указаны");
        }
        if (!dto.getStart().isBefore(dto.getEnd())) {
            throw new BadRequestException("Конец не может быть раньше или равен началу");
        }
        if (dto.getStart().isBefore(LocalDateTime.now()) || dto.getEnd().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Даты не могут быть в прошлом");
        }

        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        return toDto(saved);
    }

    @Override
    public BookingDto approveBooking(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец вещи может подтвердить бронирование");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new BadRequestException("Бронирование уже подтверждено или отклонено");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updated = bookingRepository.save(booking);
        return toDto(updated);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwnerId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new ForbiddenException("Доступ запрещён");
        }

        return toDto(booking);
    }

    @Override
    public List<BookingDto> getAllOwnByBooker(Long bookerId, String state, int from, int size) {
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException("Пользователь с id=" + bookerId + " не найден");
        }
        if (size <= 0) {
            throw new BadRequestException("size должен быть > 0");
        }
        if (from < 0) {
            throw new BadRequestException("from не может быть < 0");
        }

        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(bookerId, pageRequest);
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllOwnByOwner(Long ownerId, String state, int from, int size) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с id=" + ownerId + " не найден");
        }
        if (size <= 0) {
            throw new BadRequestException("size должен быть > 0");
        }
        if (from < 0) {
            throw new BadRequestException("from не может быть < 0");
        }

        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId, pageRequest);
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .item(new BookingItemDto(
                        booking.getItem().getId(),
                        booking.getItem().getName()))
                .booker(new BookingBookerDto(booking.getBooker().getId()))
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }
}