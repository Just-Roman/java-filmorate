package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class FilmService {

    private InMemoryFilmStorage inMemoryFilmStorage;
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

    public Film addLike(int filmId, int userId) {
        User user = inMemoryUserStorage.getUserById(userId);
        Film film = inMemoryFilmStorage.getFilmById(filmId);
        return inMemoryFilmStorage.addLike(film, user);
    }

    public Film removeLike(int filmId, int userId) {
        return inMemoryFilmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getFilmsByLike(Integer sizeFilms) {
        return inMemoryFilmStorage.getFilmsByLike(sizeFilms);
    }

}
