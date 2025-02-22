package ru.practicum.ewm.compilations.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompilationDto {
    List<Long> events;

    @Getter
    Boolean pinned;

    @Size(min = 1, max = 50)
    String title;

}
