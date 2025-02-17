package ru.practicum.ewm.requests.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.requests.enums.RequestStatus;
import ru.practicum.ewm.stats.util.Constants;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {
    private Long id;

    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime created;

    private Long event;

    private Long requester;

    private RequestStatus status;
}
