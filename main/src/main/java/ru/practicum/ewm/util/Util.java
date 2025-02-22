package ru.practicum.ewm.util;

import jakarta.validation.ValidationException;

import java.time.LocalDateTime;

public class Util {
    public static void checkActualTime(LocalDateTime eventTime) {
        if (eventTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Incorrect request.");
        }
    }
}
