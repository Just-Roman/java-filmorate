package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {



/*        public static Film mapToUser(NewUserRequest request) {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setEmail(request.getEmail());
            user.setRegistrationDate(Instant.now());

            return user;
        }

        public static UserDto mapToUserDto(User user) {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setRegistrationDate(Instant.now());
            return dto;
        }

        public static User updateUserFields(User user, UpdateUserRequest request) {
            if (request.hasEmail()) {
                user.setEmail(request.getEmail());
            }
            if (request.hasPassword()) {
                user.setPassword(request.getPassword());
            }
            if (request.hasUsername()) {
                user.setUsername(request.getUsername());
            }
            return user;
        }*/


}
