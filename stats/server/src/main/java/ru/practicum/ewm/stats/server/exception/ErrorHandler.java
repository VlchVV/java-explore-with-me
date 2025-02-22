package ru.practicum.ewm.stats.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.DateTimeException;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static String stackTraceToString(Throwable throwable) {
        Writer buffer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(buffer);
        throwable.printStackTrace(printWriter);
        return buffer.toString();
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, DateTimeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(RuntimeException e) {
        log.error(stackTraceToString(e));
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrect request.")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServerError(final Exception exception) {
        log.error(stackTraceToString(exception));
        return ApiError.builder()
                .message(exception.getMessage())
                .status("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
