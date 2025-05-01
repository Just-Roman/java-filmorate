package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingDbStorage mpaRatingDbStorage;

    public MpaRating getById(Integer id) {
        return mpaRatingDbStorage.geById(id);
    }

    public Collection<MpaRating> getAll() {
        return mpaRatingDbStorage.getAll();
    }

}
