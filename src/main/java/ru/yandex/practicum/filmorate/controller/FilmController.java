package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    @Autowired
    private FilmService filmService;

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/popular")
    public Collection<Film> getFilmsByLike(@RequestParam(defaultValue = "10") int sizeFilms) {
        return filmService.getFilmsByLike(sizeFilms);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film filmUpdate) {
        return filmService.update(filmUpdate);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public Map<Film, Set<Integer>> addLike(@PathVariable int filmId, @PathVariable int userId) {
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable int filmId, @PathVariable int userId) {
        filmService.removeLike(filmId, userId);
    }


}
