package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final InMemoryUserStorage inMemoryUserStorage;

    private final Map<Integer, Film> films = new HashMap<>();
    private final Map<Integer, Set<Integer>> filmsLikes = new HashMap<>();
    private final LocalDate birthdayFilm = LocalDate.of(1895, 12, 28);

    private int id = 1;

    public int getNextId() {
        return id++;
    }


    @Override
    public Collection<Film> getAll() {
        log.info("GET, all films");
        return films.values();
    }

    @Override
    public Film getFilmById(int id) {
        validateFilmId(id);
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        log.info("POST, create film {}", film);
        validateReleaseDate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film filmUpdate) {
        log.info("PUT, update film {}", filmUpdate);
        Integer id = validateUpdate(filmUpdate);
        Film oldFilm = films.get(id);
        String newName = filmUpdate.getName();
        if (!newName.equals(oldFilm.getName())) {
            oldFilm.setName(newName);
        }
        if (filmUpdate.getDescription() != null) {
            String newDescription = filmUpdate.getDescription();
            if (!newDescription.equals(oldFilm.getDescription())) {
                oldFilm.setDescription(newDescription);
            }
        }
        LocalDate newReleaseDate = filmUpdate.getReleaseDate();
        if (!newReleaseDate.equals(oldFilm.getReleaseDate())) {
            oldFilm.setReleaseDate(newReleaseDate);
        }

        if (filmUpdate.getDuration() != null) {
            Integer newDuration = filmUpdate.getDuration();
            if (!newDuration.equals(oldFilm.getDuration())) {
                oldFilm.setDuration(newDuration);
            }
        }
        return oldFilm;
    }

    @Override
    public Map<Film, Set<Integer>> addLike(int filmId, int userId) {
        inMemoryUserStorage.validateUserId(userId);
        validateFilmId(filmId);

        if (checkFilmsLikes(filmId)) {
            Set<Integer> likes = filmsLikes.get(filmId);

            if (likes.add(userId)) {
                addLikeFilm(filmId);
            }
        } else {
            addLikeFilm(filmId);

            Set<Integer> likes = new HashSet<>();
            likes.add(userId);
            filmsLikes.put(filmId, likes);
        }

        return Map.of(films.get(filmId), filmsLikes.get(filmId));
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        inMemoryUserStorage.validateUserId(userId);
        validateFilmId(filmId);

        if (checkFilmsLikes(filmId)) {
            Set<Integer> likes = filmsLikes.get(filmId);
            likes.remove(userId);
            removeLikeFilm(filmId);
            if (likes.isEmpty()) {
                filmsLikes.remove(filmId);
            }
        }
    }

    @Override
    public Collection<Film> getFilmsByLike(Integer sizeFilms) {
        return films.values()
                .stream()
                .sorted(Comparator.comparing(Film::getLikes).reversed())
                .limit(sizeFilms)
                .collect(Collectors.toList());
    }

    private void validateFilmId(Integer id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id: " + id + " не найден");
        }
    }


    private boolean checkFilmsLikes(int id) {
        return filmsLikes.containsKey(id);
    }

    private void addLikeFilm(int filmId) {
        Film film = films.get(filmId);
        film.setLikes(film.getLikes() + 1);
    }

    private void removeLikeFilm(int filmId) {
        Film film = films.get(filmId);
        int like = film.getLikes();

        if (like != 0) {
            film.setLikes(like - 1);
        }
    }

    private void validateReleaseDate(Film film) {
        log.info("validateReleaseDate start for {}", film);
        if (film.getReleaseDate().isBefore(birthdayFilm)) {
            String msg = "дата релиза — не раньше 28 декабря 1895 года";
            log.error(msg);
            throw new ValidationException(msg);
        }

    }

    private Integer validateUpdate(Film film) {
        log.info("validateUpdate start for {}", film);
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
        }
        validateReleaseDate(film);
        return id;
    }

}
