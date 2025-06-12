package ru.practicum.shareit.item;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT i FROM Item i WHERE " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND i.available = true")
    List<Item> searchByText(String text, Pageable pageable);

    List<Item> findByRequestId(Long requestId);

    List<Item> findByOwnerIdOrderById(Long userId, PageRequest pageRequest);

    List<Item> findByRequestIdOrderById(Long requestId);
}