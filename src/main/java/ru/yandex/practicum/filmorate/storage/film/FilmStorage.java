package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {

    Collection<Film> getAll();

    Film getFilmById(int id);

    Film create(Film film);

    Film update(Film filmUpdate);

    Map<Film, Set<Integer>> addLike(int filmId, int userId);

    void removeLike(Integer filmId, Integer userId);

    Collection<Film> getFilmsByLike(Integer sizeFilms);

    void validateFilmId(Integer id);

    boolean checkFilmsLikes(int id);

    void validateReleaseDate(Film film);

    Integer validateUpdate(Film film);

}
