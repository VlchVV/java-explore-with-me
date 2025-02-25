package ru.practicum.ewm.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.stats.util.Constants;
import ru.practicum.ewm.users.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    String text;
    UserShortDto author;
    EventShortDto event;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    LocalDateTime created;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    LocalDateTime edited;
}
