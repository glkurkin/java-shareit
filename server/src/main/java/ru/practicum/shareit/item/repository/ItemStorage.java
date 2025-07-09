package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Integer> {
    List<Item> findAllByOwnerId(Integer ownerId);

    @Query("select i from Item i " +
           "where upper(i.name) like upper(concat('%', :searchText, '%')) " +
           "or upper(i.description) like upper(concat('%', :searchText, '%')) " +
           "and i.available = true")
    List<Item> findBySearchText(@Param("searchText") String searchText);

    @Query("SELECT i FROM Item i JOIN i.itemRequest r WHERE r.id IN :requestIds")
    List<Item> findByRequestIdIn(List<Integer> requestIds);
}
