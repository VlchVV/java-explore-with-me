package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.stats.util.Constants;
import ru.practicum.ewm.users.dto.UserShortDto;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    Long id;

    String annotation;

    CategoryDto category;

    Long confirmedRequests;

    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    UserShortDto initiator;

    Boolean paid;

    String title;
}
