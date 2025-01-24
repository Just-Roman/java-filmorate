package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmTest {

    private static final Validator validator;

    static {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    private InMemoryFilmStorage inMemoryFilmStorage;

    @BeforeEach
    public void setUp() {
        inMemoryFilmStorage = new InMemoryFilmStorage();

    }

    //    пустое имя
    @Test
    public void whenFilmNameIsEmptyThenThrow() {
        Film film = Film.builder()
                .name("")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1)
                .build();
//        Пусто
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        assertEquals(1, violation.size());
    }

    //    больше максимальной вместимости описания
    @Test
    public void whenFilmDescriptionMoreMaxSizeThrow() {
        String twoHundredOneSymbols = "123456789012345678901234567890123456789" +
                "0123456789012345678901234567890123456789012345678901234567890" +
                "123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901";
        String twoHundredSymbols = "123456789012345678901234567890123456789" +
                "0123456789012345678901234567890123456789012345678901234567890" +
                "123456789012345678901234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890";
        Film film = Film.builder()
                .name("Super")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .description(twoHundredOneSymbols)
                .duration(1)
                .build();
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        assertEquals(1, violation.size());

        film.setDescription(twoHundredSymbols);
        Set<ConstraintViolation<Film>> violation2 = validator.validate(film);
        assertEquals(0, violation2.size());
    }

    //    дата релиза — не раньше 28 декабря 1895 года;
    @Test
    public void whenFilmReleaseDateBeforeBirthdayFilmsThrowValidationException() {
        Film film = Film.builder()
                .name("Super")
                .description("Super description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .build();
        assertThrows(ValidationException.class, () -> inMemoryFilmStorage.create(film));

        film.setName("Fork");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertDoesNotThrow(() -> inMemoryFilmStorage.create(film));
    }

    //    продолжительность фильма должна быть положительным числом.
    @Test
    public void filmDurationOnlyPositive() {
        Film film = Film.builder()
                .name("Super")
                .description("Super description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(-1)
                .build();
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        assertEquals(1, violation.size());

        film.setDuration(1);
        Set<ConstraintViolation<Film>> violation2 = validator.validate(film);
        assertEquals(0, violation2.size());
    }

    //  Правильный сценарий создания
    @Test
    public void filmCreate() {
        Film film = Film.builder()
                .name("Super")
                .description("Super description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1)
                .build();
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        assertEquals(0, violation.size());
        assertDoesNotThrow(() -> inMemoryFilmStorage.create(film));
    }

    //  Правильный сценарий обновления
    @Test
    public void filmUpdate() {
//        сохраняем
        Film film = Film.builder()
                .name("Super")
                .description("Super description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1)
                .build();
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        assertEquals(0, violation.size());
        assertDoesNotThrow(() -> inMemoryFilmStorage.create(film));

//        обновляем
        film.setId(1);
        film.setName("Super++");
        film.setDescription("Super description++");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(2);
        Set<ConstraintViolation<Film>> violation2 = validator.validate(film);
        assertEquals(0, violation2.size());
        assertDoesNotThrow(() -> inMemoryFilmStorage.update(film));
        Film createdFilm = inMemoryFilmStorage.update(film);
        assertEquals(film.toString(), createdFilm.toString());
    }


}
