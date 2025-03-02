package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Friendship {

    @NotNull
    private Integer firstUserId;
    @NotNull
    private Integer secondUserId;
    @NotNull
    private Boolean status;

}
