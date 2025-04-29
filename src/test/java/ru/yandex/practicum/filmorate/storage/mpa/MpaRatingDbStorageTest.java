package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreDbStorage.class,
        MpaRatingDbStorage.class, UserDbStorage.class, MpaRatingDbStorage.class})
class MpaRatingDbStorageTest {

    private final MpaRatingDbStorage mpaRatingDbStorage;

    @Test
    void geById() {
        MpaRating mpa = mpaRatingDbStorage.geById(1);

        Assertions.assertThat(mpa.getName()).isEqualTo("G");
    }

    @Test
    void getAll() {
        List<MpaRating> ratings = (List<MpaRating>) mpaRatingDbStorage.getAll();

        Assertions.assertThat(ratings.get(0).getName()).isEqualTo("G");
        Assertions.assertThat(ratings.get(1).getName()).isEqualTo("PG");
        Assertions.assertThat(ratings.get(2).getName()).isEqualTo("PG-13");
        Assertions.assertThat(ratings.get(3).getName()).isEqualTo("R");
        Assertions.assertThat(ratings.get(4).getName()).isEqualTo("NC-17");
    }
}