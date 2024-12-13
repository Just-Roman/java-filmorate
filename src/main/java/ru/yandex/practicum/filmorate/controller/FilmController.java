package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAll() {
        log.debug("GET, all films");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.debug("POST, create film {}", film);
        validateCloneName(film);
        validateReleaseDate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }


    @PutMapping
    public Film update(@Valid @RequestBody Film filmUpdate) {
        log.debug("PUT, update film {}", filmUpdate);
        Integer id = validateUpdate(filmUpdate);
        Film oldFilm = films.get(id);
        String newName = filmUpdate.getName();
        if (!newName.equals(oldFilm.getName())) oldFilm.setName(newName);
        if (filmUpdate.getDescription() != null) {
            String newDescription = filmUpdate.getDescription();
            if (!newDescription.equals(oldFilm.getDescription())) oldFilm.setDescription(newDescription);
        }
        if (filmUpdate.getReleaseDate() != null) {
            LocalDate newReleaseDate = filmUpdate.getReleaseDate();
            if (!newReleaseDate.equals(oldFilm.getReleaseDate())) oldFilm.setReleaseDate(newReleaseDate);
        }
        if (filmUpdate.getDuration() != null) {
            Integer newDuration = filmUpdate.getDuration();
            if (!newDuration.equals(oldFilm.getDuration())) oldFilm.setDuration(newDuration);
        }
        return oldFilm;
    }


    private void validateReleaseDate(Film film) {
        log.debug("validateReleaseDate start for {}", film);
        LocalDate birthdayFilm = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(birthdayFilm)) {
                String msg = "дата релиза — не раньше 28 декабря 1895 года";
                log.error(msg);
                throw new ValidationException(msg);
            }
        }
    }

    private void validateCloneName(Film film) {
        log.debug("validateCloneName start for {}", film);
        String name = film.getName();
        for (Film filmMap : films.values()) {
            if (filmMap.getName().equals(name)) {
                String msg = "Название фильма уже есть в базе";
                log.error(msg);
                throw new ValidationException(msg);
            }
        }
    }

    private Integer validateUpdate(Film film) {
        log.debug("validateUpdate start for {}", film);
        String newName = film.getName();
        if (film.getId() == null) {
            String msg = "Id должен быть указан";
            log.error(msg);
            throw new ValidationException(msg);
        }
        Integer id = film.getId();
        if (!films.containsKey(id)) {
            String msg = "Фильм с id = " + id + " не найден";
            log.error(msg);
            throw new NotFoundException(msg);
        } else if (!films.get(id).getName().equals(newName)) {
            validateCloneName(film);
        }
        validateReleaseDate(film);
        return id;
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
