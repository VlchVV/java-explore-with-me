package ru.practicum.ewm.stats.server.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.stats.util.Constants;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApiError {
    private final List<String> errors;
    private final String message;
    private final String reason;
    private final String status;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private final LocalDateTime timestamp;
}
