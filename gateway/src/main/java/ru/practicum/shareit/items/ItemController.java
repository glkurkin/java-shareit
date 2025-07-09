package ru.practicum.shareit.items;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.items.dto.CommentCreateDto;
import ru.practicum.shareit.items.dto.ItemCreateDto;
import ru.practicum.shareit.items.dto.ItemUpdateDto;

@Slf4j
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;
    private static final String USERID_HEADER = "X-Sharer-User-Id";

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(USERID_HEADER) Integer userId, @PathVariable(required = false) Integer itemId) {
        log.info("Выводим предмет с ИД = {}", itemId);
        return itemClient.findById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemByUserId(@RequestHeader(USERID_HEADER) Integer userId) {
        log.info("Выводим предмет пользователя с ИД = {}", userId);
        return itemClient.findByUserId(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchByText(@RequestParam String text,
                                               @RequestHeader(USERID_HEADER) Long userId) {
        log.info("Ищем предмет с описанием {}", text);
        return itemClient.searchByText(text, userId);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(USERID_HEADER) Integer userId, @Valid @RequestBody ItemCreateDto itemDto) {
        log.info("Добавляем предмет {} от пользователя c ИД = {}", itemDto, userId);
        return itemClient.addItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USERID_HEADER) Integer userId, @Valid @RequestBody CommentCreateDto commentCreateDto,
                                             @PathVariable Integer itemId) {
        log.info("Добавляем комментарий {} от пользователя с ИД = {} к предмету с ИД = {}", commentCreateDto, userId, itemId);
        return itemClient.addComment(userId, commentCreateDto, itemId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updItem(@RequestHeader(USERID_HEADER) Integer userId, @Valid @RequestBody ItemUpdateDto iUpdateDto,
                                          @PathVariable Integer id) {
        log.info("Обновляем предмет с ИД = {} пользователя с ИД = {}", id, userId);
        return itemClient.updItem(userId, iUpdateDto, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable Integer id) {
        log.info("Удаляем пользователя с ИД = {}", id);
        return itemClient.deleteItem(id);
    }
}
