package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStorage extends JpaRepository<Booking, Integer> {
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.booker.id = :userId AND " +
            "b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED AND b.end < :currentTime")
    List<Booking> findAllApprovedByItemIdAndBookerId(long itemId, long userId, LocalDateTime currentTime);

    List<Booking> findAllByItemId(Integer id);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findAllByItem_Owner_IdOrderByStartDesc(@Param("ownerId") Integer ownerId);

    List<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Integer ownerId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByItem_Owner_IdAndEndBeforeOrderByStartDesc(Integer ownerId, LocalDateTime now);

    List<Booking> findByItem_Owner_IdAndStartAfterOrderByStartDesc(Integer ownerId, LocalDateTime now);

    List<Booking> findAllByItem_Owner_IdAndStatusOrderByStartDesc(Integer ownerId, BookingStatus status);

}
