package ru.practicum.ewm.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCompilationDto {

    List<Long> events;

    boolean pinned = false;

    @NotBlank
    @Size(min = 1, max = 50)
    String title;

    public Boolean getPinned() {
        return pinned;
    }
}
