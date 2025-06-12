package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
class BookingRequestDtoJsonTest {

    @SpringBootConfiguration
    static class TestConfig {
    }

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Test
    void serialize_CorrectJson() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 6, 20, 12, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 22, 12, 0);
        BookingRequestDto dto = new BookingRequestDto(42L, start, end);

        JsonContent<BookingRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("@.itemId");
        assertThat(result).extractingJsonPathNumberValue("@.itemId").isEqualTo(42);
        assertThat(result).hasJsonPathStringValue("@.start");
        assertThat(result).extractingJsonPathStringValue("@.start")
                .startsWith("2025-06-20T12:00");
        assertThat(result).hasJsonPathStringValue("@.end");
        assertThat(result).extractingJsonPathStringValue("@.end")
                .startsWith("2025-06-22T12:00");
    }

    @Test
    void deserialize_CorrectObject() throws Exception {
        String content = """
                {
                  "itemId": 7,
                  "start": "2025-07-01T09:30:00",
                  "end":   "2025-07-02T09:30:00"
                }
                """;

        BookingRequestDto dto = json.parse(content).getObject();
        assertThat(dto.getItemId()).isEqualTo(7L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 7, 1, 9, 30));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 7, 2, 9, 30));
    }
}
