package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FilmStorage {

    Collection<Film> getAll();

    Film getFilmById(int id);

    Film create(Film film);

    Film update(Film filmUpdate);

    Film addLike(Film film, User user);

    Film removeLike(Integer filmId, Integer userId);

    Collection<Film> getFilmsByLike(Integer sizeFilms);

    void validateFilmId(Integer id);

    void validateReleaseDate(Film film);

    Integer validateUpdate(Film film);

}
