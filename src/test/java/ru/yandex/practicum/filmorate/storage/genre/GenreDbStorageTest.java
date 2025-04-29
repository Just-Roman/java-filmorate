package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreDbStorage.class,
        MpaRatingDbStorage.class, UserDbStorage.class, GenreDbStorage.class})
class GenreDbStorageTest {

    private final GenreDbStorage genreDbStorage;

    @Test
    void getById() {
//        Act
        Genre genre = genreDbStorage.getById(1);

//        Assert
        Assertions.assertThat(genre.getName()).isEqualTo("Комедия");
    }

    @Test
    void getAll() {
//        Act
        List<Genre> genres = (List<Genre>) genreDbStorage.getAll();

//        Assert
        Assertions.assertThat(genres.get(0).getName()).isEqualTo("Комедия");
        Assertions.assertThat(genres.get(1).getName()).isEqualTo("Драма");
        Assertions.assertThat(genres.get(2).getName()).isEqualTo("Мультфильм");
        Assertions.assertThat(genres.get(3).getName()).isEqualTo("Триллер");
        Assertions.assertThat(genres.get(4).getName()).isEqualTo("Документальный");
        Assertions.assertThat(genres.get(5).getName()).isEqualTo("Боевик");
    }
}