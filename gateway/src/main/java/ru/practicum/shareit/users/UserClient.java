package ru.practicum.shareit.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.users.dto.UserCreateDto;
import ru.practicum.shareit.users.dto.UserUpdateDto;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String url, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(url + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> findById(Integer id) {
        return get("/" + id);
    }

    public ResponseEntity<Object> getAllUsers() {
        return getAll("");
    }

    public ResponseEntity<Object> addUser(UserCreateDto uCreateDto) {
        return post("", uCreateDto);
    }

    public ResponseEntity<Object> updUser(UserUpdateDto uUpdateDto, Integer id) {
        return patch("/" + id, id, uUpdateDto);
    }

    public ResponseEntity<Object> deleteUser(Integer id) {
        return delete("/" + id, id);
    }
}
