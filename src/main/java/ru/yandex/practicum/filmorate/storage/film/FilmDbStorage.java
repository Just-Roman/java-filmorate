package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Repository
public class FilmDbStorage implements FilmStorage {
    protected final JdbcTemplate jdbc;

    public FilmDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final LocalDate birthdayFilm = LocalDate.of(1895, 12, 28);

    private static final String GET_BY_ID = """
               SELECT
                   f.id AS film_id,
                   f.name AS film_name,
                   f.description,
                   f.release_date,
                   f.duration_minutes,
                   f.likes_count,
                   f.rating AS mpa_id,
                   COUNT(fl.user_id) AS likes_count,
                   COALESCE(STRING_AGG(fg.genre_id, ', '), '') AS genre_ids\s
               FROM
                   film f
               LEFT JOIN
                   film_like fl ON f.id = fl.film_id
               LEFT JOIN
                   film_genre fg ON f.id = fg.film_id
                   WHERE f.id = ?
               GROUP BY
                   f.id, f.name, f.description, f.release_date, f.duration_minutes, f.likes_count, f.rating;
            """;

    private static final String GET_ALL = """
            SELECT\s
                   f.id AS film_id,
                   f.name AS film_name,
                   f.description,
                   f.release_date,
                   f.duration_minutes,
                   f.likes_count,
                   f.rating AS mpa_id,
                   COUNT(fl.user_id) AS likes_count,
                   COALESCE(STRING_AGG(fg.genre_id, ', '), '') AS genre_ids
               FROM\s
                   film f
               LEFT JOIN\s
                   film_like fl ON f.id = fl.film_id
               LEFT JOIN\s
                   film_genre fg ON f.id = fg.film_id
               GROUP BY\s
                   f.id, f.name, f.description, f.release_date, f.duration_minutes, f.likes_count, f.rating
               ORDER BY\s
                   likes_count DESC;
            """;

    private static final String GET_FILM_BY_LIKES = """
            SELECT\s
                   f.id AS film_id,
                   f.name AS film_name,
                   f.description,
                   f.release_date,
                   f.duration_minutes,
                   f.likes_count,
                   f.rating AS mpa_id,
                   COUNT(fl.user_id) AS likes_count,
                   COALESCE(STRING_AGG(fg.genre_id, ', '), '') AS genre_ids
               FROM\s
                   film f
               LEFT JOIN\s
                   film_like fl ON f.id = fl.film_id
               LEFT JOIN\s
                   film_genre fg ON f.id = fg.film_id
               GROUP BY\s
                   f.id, f.name, f.description, f.release_date, f.duration_minutes, f.likes_count, f.rating
               ORDER BY\s
                   likes_count DESC
            LIMIT ?;
            """;

    private static final String INSERT_FILMS = "INSERT INTO film (name, description, release_date, " +
            "duration_minutes, rating) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_LIKE = "INSERT INTO film_like (user_id, film_id) VALUES (?, ?)";
    String UPDATE_FILM = """
             UPDATE film
             SET name = ?, description = ?, release_date = ?, duration_minutes = ?, rating = ?
             WHERE id = ?
            """;
    private static final String UPDATE_FILM_GENRE = """
            UPDATE film_genre
                 SET film_id = ?, genre_id = ?
                 WHERE id = ?
            """;

    private static final String DELETE_LIKE = "DELETE FROM film_like  WHERE user_id = ? AND film_id = ?";
    private static final String DELETE_FILM_GENRE = "DELETE FROM film_genre WHERE film_id = ?";

    private static final String GET_MAX_ID_MPA = "SELECT MAX(id) AS id FROM mpa;";
    private static final String GET_MAX_ID_GENRE = "SELECT MAX(id) AS id FROM genres;";


    @Override
    public Collection<Film> getAll() {
        return jdbc.query(GET_ALL, FilmDbStorage::getFilmMapper);
    }

    @Override
    public Film getFilmById(int id) {
        return jdbc.queryForObject(GET_BY_ID, FilmDbStorage::getFilmMapper, id);
    }

    @Override
    public Film create(Film film) {
        validateReleaseDate(film);
        validateIdMpa(film.getMpa().getId());
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_FILMS, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        Integer id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(id);

        if (film.getGenres() != null) {
            createGenre(film.getGenres(), id);
        }

        return jdbc.queryForObject(GET_BY_ID, FilmDbStorage::getFilmMapper, id);
    }

    @Override
    public Film update(Film filmUpdate) {
        int id = filmUpdate.getId();

        jdbc.update(UPDATE_FILM,
                filmUpdate.getName(),
                filmUpdate.getDescription(),
                filmUpdate.getReleaseDate(),
                filmUpdate.getDuration(),
                filmUpdate.getMpa().getId(),
                id
        );

        if (filmUpdate.getGenres() != null) {
            deleteGenre(id);
            createGenre(filmUpdate.getGenres(), id);
        }
        return jdbc.queryForObject(GET_BY_ID, FilmDbStorage::getFilmMapper, id);
    }

    @Override
    public boolean addLike(int filmId, int userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        return jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_LIKE);
            stmt.setInt(1, userId);
            stmt.setInt(2, filmId);
            return stmt;
        }, keyHolder) > 0;

    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        jdbc.update(DELETE_LIKE, userId, filmId);
    }

    @Override
    public Collection<Film> getFilmsByLike(Integer sizeFilms) {
        return Collections.singleton(jdbc.queryForObject(GET_FILM_BY_LIKES, FilmDbStorage::getFilmMapper, sizeFilms));
    }

    private static Film getFilmMapper(ResultSet resultSet, int rowNum) throws SQLException {
        Timestamp releaseDate = resultSet.getTimestamp("release_date");

        String genreIds = resultSet.getString("genre_ids");
        List<Genre> genres = new ArrayList<>();
        if (!genreIds.isEmpty()) {
            String[] genreIdsArray = genreIds.split(",\\s*");
            for (String id : genreIdsArray) {
                genres.add(Genre.builder()
                        .id(Integer.parseInt(id))
                        .build());
            }
        }

        return Film.builder()
                .id(resultSet.getInt("film_id"))
                .name(resultSet.getString("film_name"))
                .description(resultSet.getString("description"))
                .releaseDate(releaseDate.toLocalDateTime().toLocalDate())
                .duration(resultSet.getInt("duration_minutes"))
                .likes_count(resultSet.getInt("likes_count"))
                .mpa(MpaRating
                        .builder()
                        .id(resultSet.getInt("mpa_id"))
                        .build())
                .genres(genres)
                .build();
    }


    private static Integer getMaxIdMapper(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getInt("id");
    }

    private void createGenre(List<Genre> genre, Integer idFilm) {
        for (Genre genres : genre) {
            validateIdGenre(genres.getId());
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbc.update(connection -> {
                PreparedStatement stmt2 = connection.prepareStatement(INSERT_FILM_GENRE);
                stmt2.setInt(1, idFilm);
                stmt2.setInt(2, genres.getId());
                return stmt2;
            }, keyHolder);
        }
    }


    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(birthdayFilm)) {
            String msg = "дата релиза — не раньше 28 декабря 1895 года";
            throw new ValidationException(msg);
        }
    }

    private void validateIdMpa(Integer id) {
        Integer maxId = jdbc.queryForObject(GET_MAX_ID_MPA, FilmDbStorage::getMaxIdMapper);
        if (maxId == null) {
            throw new NotFoundException("Таблица mpa пустая");
        }
        if (id > maxId) {
            throw new NotFoundException("Ваш id = " + id + " в таблице mpa не найден");
        }
    }

    private void validateIdGenre(Integer id) {
        Integer maxId = jdbc.queryForObject(GET_MAX_ID_GENRE, FilmDbStorage::getMaxIdMapper);
        if (maxId == null) {
            throw new NotFoundException("Таблица genres пустая");
        }
        if (id > maxId) {
            throw new NotFoundException("Ваш id = " + id + " в таблице genres не найден");
        }
    }

    private void deleteGenre(Integer filmId) {
        jdbc.update(DELETE_FILM_GENRE, filmId);
    }


}
