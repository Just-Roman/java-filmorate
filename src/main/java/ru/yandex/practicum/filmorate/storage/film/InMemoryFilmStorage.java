package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.IdGenerator;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final IdGenerator idGenerator;
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getAll() {
//        log.debug("GET, all films");
        return films.values();
    }

    @Override
    public Film getFilmById(int id) {
        validateFilmId(id);
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
//        log.debug("POST, create film {}", film);
        validateReleaseDate(film);
        film.setId(idGenerator.getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film filmUpdate) {
//        log.debug("PUT, update film {}", filmUpdate);
        Integer id = validateUpdate(filmUpdate);
        Film oldFilm = films.get(id);
        String newName = filmUpdate.getName();
        if (!newName.equals(oldFilm.getName())) oldFilm.setName(newName);
        if (filmUpdate.getDescription() != null) {
            String newDescription = filmUpdate.getDescription();
            if (!newDescription.equals(oldFilm.getDescription())) oldFilm.setDescription(newDescription);
        }
        LocalDate newReleaseDate = filmUpdate.getReleaseDate();
        if (!newReleaseDate.equals(oldFilm.getReleaseDate())) oldFilm.setReleaseDate(newReleaseDate);

        if (filmUpdate.getDuration() != null) {
            Integer newDuration = filmUpdate.getDuration();
            if (!newDuration.equals(oldFilm.getDuration())) oldFilm.setDuration(newDuration);
        }
        return oldFilm;
    }

    @Override
    public Film addLike(Film film, User user) {
        film.getLikes().add(user.getId());
        int id = film.getId();
        films.put(id, film);
        return films.get(id);
    }

    @Override
    public Film removeLike(Integer filmId, Integer userId) {
        validateFilmId(filmId);
        Film film = films.get(filmId);
        film.getLikes().remove(userId);
        films.put(filmId, film);
        return films.get(filmId);
    }

    @Override
    public Collection<Film> getFilmsByLike(Integer sizeFilms) {

        List<Film> filmsSortedByLikes = films.values()
                .stream()
                .sorted(Comparator.comparingInt(Film::getLikesSize))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Film> returnSortedFilms = new ArrayList<>();

        for (int i = 0; i < sizeFilms; i++) {
            if (filmsSortedByLikes.size() < i + 1) break;
            returnSortedFilms.add(filmsSortedByLikes.get(i));
        }
        return returnSortedFilms;
    }

    @Override
    public void validateFilmId(Integer id) {
        if (!films.containsKey(id)) throw new NotFoundException("Фильм с id=%d не найден");
    }

    @Override
    public void validateReleaseDate(Film film) {
//        log.debug("validateReleaseDate start for {}", film);
        LocalDate birthdayFilm = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(birthdayFilm)) {
            String msg = "дата релиза — не раньше 28 декабря 1895 года";
//            log.error(msg);
            throw new ValidationException(msg);
        }

    }

    @Override
    public Integer validateUpdate(Film film) {
//        log.debug("validateUpdate start for {}", film);
        String newName = film.getName();
        if (film.getId() == null) {
            String msg = "Id должен быть указан";
//            log.error(msg);
            throw new ValidationException(msg);
        }
        Integer id = film.getId();
        if (!films.containsKey(id)) {
            String msg = "Фильм с id = " + id + " не найден";
//            log.error(msg);
            throw new NotFoundException(msg);
        }
        validateReleaseDate(film);
        return id;
    }

}
