package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class FilmService {

    @Autowired
    private InMemoryFilmStorage inMemoryFilmStorage;
    @Autowired
    private InMemoryUserStorage inMemoryUserStorage;

    public Collection<Film> getAll() {
        return inMemoryFilmStorage.getAll();
    }

    public Film create(Film film) {
        return inMemoryFilmStorage.create(film);
    }

    public Film update(Film filmUpdate) {
        return inMemoryFilmStorage.update(filmUpdate);
    }

    public Map<Film, Set<Integer>> addLike(int filmId, int userId) {
        inMemoryUserStorage.validateUserId(userId);
        return inMemoryFilmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        inMemoryUserStorage.validateUserId(userId);
        inMemoryFilmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getFilmsByLike(Integer sizeFilms) {
        return inMemoryFilmStorage.getFilmsByLike(sizeFilms);
    }

}
