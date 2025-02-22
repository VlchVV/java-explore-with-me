package ru.practicum.ewm.users;

import ru.practicum.ewm.users.dto.UserDto;
import ru.practicum.ewm.users.dto.UserNewDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserNewDto userNewDto);

    List<UserDto> getUsers(List<Long> userIds, Integer from, Integer size);

    void deleteUser(Long userId);
}
