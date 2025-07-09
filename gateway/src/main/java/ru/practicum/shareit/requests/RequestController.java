package ru.practicum.shareit.requests;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestClient requestClient;
    private static final String USERID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader(USERID_HEADER) Integer userId,
                                             @Valid @RequestBody RequestDto requestDto) {
        log.info("Добавляем запрос {} от пользователя с ИД = {}", requestDto, userId);
        return requestClient.addRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getByUserId(@RequestHeader(USERID_HEADER) Integer userId) {
        log.info("Получаем запросы пользователя с ИД = {} ", userId);
        return requestClient.findAllRequestByUserId(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getByRequestId(@PathVariable Integer requestId) {
        log.info("Получаем запрос с ИД = {}", requestId);
        return requestClient.findRequestById(requestId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests() {
        log.info("Выводим все запросы");
        return requestClient.findAllRequests();
    }
}
