package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmTest {

    private static final Validator validator;

    static {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    private FilmController filmController;

    @BeforeEach
    public void setUp() {
        filmController = new FilmController();

    }

    //    пустое имя
    @Test
    public void whenFilmNameIsEmptyThenThrow() {
        Film film = Film.builder()
                .name(" ")
                .build();
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        assertEquals(1, violation.size());
    }

//    больше максимальной вместимости
    @Test
    public void whenFilmDescriptionMoreMaxSizeThrow() {
        String twoHundredSymbols = "123456789012345678901234567890123456789" +
                "0123456789012345678901234567890123456789012345678901234567890" +
                "123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901";
        Film film = Film.builder()
                .name("Super")
                .description(twoHundredSymbols)
                .build();
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        assertEquals(1, violation.size());
    }

//    дата релиза — не раньше 28 декабря 1895 года;
    @Test
    public void whenFilmReleaseDateBeforeBirthdayFilmsThrowValidationException() {
        Film film = Film.builder()
                .name("Super")
                .description("Super description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .build();
        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

}
