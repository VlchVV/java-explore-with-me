package ru.practicum.ewm.requests.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.requests.enums.RequestStatus;
import ru.practicum.ewm.stats.util.Constants;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestDto {
    Long id;

    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    LocalDateTime created;

    Long event;

    Long requester;

    RequestStatus status;
}
