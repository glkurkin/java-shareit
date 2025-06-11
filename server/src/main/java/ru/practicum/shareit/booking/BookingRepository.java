package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.ownerId = :ownerId ORDER BY b.start DESC")
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);
}