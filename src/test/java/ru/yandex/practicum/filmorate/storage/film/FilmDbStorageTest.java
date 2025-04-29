package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreDbStorage.class, MpaRatingDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    Film film1 = Film.builder()
            .name("iron man")
            .description("Super description")
            .releaseDate(LocalDate.of(1895, 12, 28))
            .duration(1)
            .genres(List.of(Genre.builder().id(1).build()))
            .mpa(MpaRating.builder().id(2).build())
            .build();

    Film film2 = Film.builder()
            .name("spider man")
            .description("Super man")
            .releaseDate(LocalDate.of(1896, 12, 29))
            .duration(1)
            .genres(List.of(Genre.builder().id(3).build()))
            .mpa(MpaRating.builder().id(4).build())
            .build();

    User user1 = User.builder()
            .email("yandex@mail.ru")
            .login("turbo")
            .birthday(LocalDate.of(2024, 12, 12))
            .build();

    User user2 = User.builder()
            .email("Arrange@mail.ru")
            .login("Away")
            .birthday(LocalDate.of(2024, 11, 11))
            .build();

    @Test
    void getAll() {
//        Arrange
        Film createdFilm1 = filmDbStorage.create(film1);
        Film createdFilm2 = filmDbStorage.create(film2);

//        Act
        Collection<Film> getAllFilms = filmDbStorage.getAll();

//        Assert
        Assertions.assertThat(getAllFilms.size()).isEqualTo(2);
        Assertions.assertThat(getAllFilms).isEqualTo(List.of(createdFilm1, createdFilm2));
    }

    @Test
    void getFilmById() {
//        Arrange
        Film createdFilm = filmDbStorage.create(film1);

//        Act
        Film film = filmDbStorage.getFilmById(createdFilm.getId());

//        Assert
        Assertions.assertThat(createdFilm).isEqualTo(film);
    }

    @Test
    void create() {
//        Act
        Film createdFilm = filmDbStorage.create(film1);

//        Assert
        Assertions.assertThat(createdFilm.getName()).isEqualTo("iron man");
        Assertions.assertThat(createdFilm.getDescription()).isEqualTo("Super description");
        Assertions.assertThat(createdFilm.getGenres().size()).isEqualTo(1);
        Assertions.assertThat(createdFilm.getMpa().getId()).isEqualTo(2);
    }

    @Test
    void update() {
//        Arrange
        Film createdFilm = filmDbStorage.create(film1);
        createdFilm.setName("The Pirates of Somalia");
        createdFilm.setDescription("tell their story");

//        Act
        Film updatedFilm = filmDbStorage.update(createdFilm);

//        Assert
        Assertions.assertThat(createdFilm.getId()).isEqualTo(updatedFilm.getId());
        Assertions.assertThat(updatedFilm.getName()).isEqualTo("The Pirates of Somalia");
        Assertions.assertThat(updatedFilm.getDescription()).isEqualTo("tell their story");
    }

    @Test
    void addLike() {
//        Arrange
        Film createdFilm = filmDbStorage.create(film1);
        User createdUser = userDbStorage.create(user1);

//        Act
        Boolean addLike = filmDbStorage.addLike(createdFilm.getId(), createdUser.getId());

//        Assert
        Assertions.assertThat(addLike).isEqualTo(true);
    }

    @Test
    void removeLike() {
//        Arrange
        Integer createdFilmId = filmDbStorage.create(film1).getId();
        Integer createdUserId = userDbStorage.create(user1).getId();
        Boolean addLike = filmDbStorage.addLike(createdFilmId, createdUserId);
        Assertions.assertThat(addLike).isEqualTo(true);

//        Act
        filmDbStorage.removeLike(createdFilmId, createdUserId);
        Film film = filmDbStorage.getFilmById(createdFilmId);

//        Assert
        Assertions.assertThat(film.getLikes_count()).isEqualTo(0);
    }

    @Test
    void getFilmsByLike() {
//        Arrange
        Integer createdFilmId1 = filmDbStorage.create(film1).getId();
        Integer createdFilmId2 = filmDbStorage.create(film2).getId();
        Integer createdUserId1 = userDbStorage.create(user1).getId();
        Integer createdUserId2 = userDbStorage.create(user2).getId();

        Assertions.assertThat(filmDbStorage.addLike(createdFilmId1, createdUserId1)).isEqualTo(true);
        Assertions.assertThat(filmDbStorage.addLike(createdFilmId1, createdUserId2)).isEqualTo(true);

//        Act
        List<Film> filmsByLike = (List<Film>) filmDbStorage.getFilmsByLike(10);

//        Assert
        Assertions.assertThat(filmsByLike.size()).isEqualTo(2);
        Assertions.assertThat(filmsByLike.getFirst().getLikes_count()).isEqualTo(2);
        Assertions.assertThat(filmsByLike.getLast().getLikes_count()).isEqualTo(0);
    }
}