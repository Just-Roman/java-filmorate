package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    public void setUp() {
        filmController = new FilmController();

    }

    @Test
    public void whenFilmNameIsEmptyThenThrowValidationException() {
        Film film = Film.builder()
                .name("")
                .build();
        filmController.create(film);
        assertThrows(ValidationException.class, () -> filmController.create(film));

        System.out.println();
    }

    @Test
    void getAll() {
    }

    @Test
    void create() {
    }

    @Test
    void update() {
    }
}