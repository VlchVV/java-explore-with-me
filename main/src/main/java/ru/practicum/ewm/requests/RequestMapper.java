package ru.practicum.ewm.requests;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.requests.dto.RequestDto;

@UtilityClass
public class RequestMapper {
    public RequestDto toRequestDto(Request request) {
        return new RequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }
}
