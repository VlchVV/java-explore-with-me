package ru.practicum.ewm.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.stats.util.Constants;
import ru.practicum.ewm.users.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private UserShortDto author;
    private EventShortDto event;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime created;
    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime edited;
}
