package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Repository
public class FilmDbStorage implements FilmStorage {
    protected final JdbcTemplate jdbc;
    private final FilmRowMapper rowMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper rowMapper) {
        this.jdbc = jdbc;
        this.rowMapper = rowMapper;
    }

    private final LocalDate birthdayFilm = LocalDate.of(1895, 12, 28);

    private static final String GET_GENERAL = """
            SELECT
                   f.id AS film_id,
                   f.name AS film_name,
                   f.description,
                   f.release_date,
                   f.duration_minutes,
            """;

    private static final String GET_BY_ID = GET_GENERAL + """
                   f.likes_count,
                   f.rating AS mpa_id,
                   COUNT(fl.user_id) AS likes_count,
                   COALESCE(STRING_AGG(fg.genre_id, ', '), '') AS genre_ids
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

    private static final String GET_ALL = GET_GENERAL + """
                   f.likes_count,
                   f.rating AS mpa_id,
                   COUNT(fl.user_id) AS likes_count,
                   COALESCE(STRING_AGG(fg.genre_id, ', '), '') AS genre_ids
               FROM
                   film f
               LEFT JOIN
                   film_like fl ON f.id = fl.film_id
               LEFT JOIN
                   film_genre fg ON f.id = fg.film_id
               GROUP BY
                   f.id, f.name, f.description, f.release_date, f.duration_minutes, f.likes_count, f.rating
               ORDER BY
                   likes_count DESC;
            """;

    private static final String GET_FILM_BY_LIKES = GET_GENERAL + """
                   f.rating AS mpa_id,
                   COUNT(fl.user_id) AS likes_count,
                   COALESCE(STRING_AGG(fg.genre_id, ', '), '') AS genre_ids
            FROM film f
            LEFT JOIN film_like fl ON f.id = fl.film_id
            LEFT JOIN film_genre fg ON f.id = fg.film_id
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration_minutes, f.likes_count, f.rating
            ORDER BY likes_count DESC
            LIMIT ?;
            """;

    private static final String INSERT_FILMS = "INSERT INTO film (name, description, release_date, " +
            "duration_minutes, rating) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_LIKE = "INSERT INTO film_like (user_id, film_id) VALUES (?, ?)";
    private static final String UPDATE_FILM = """
             UPDATE film
             SET name = ?, description = ?, release_date = ?, duration_minutes = ?, rating = ?
             WHERE id = ?
            """;

    private static final String CHECK_DUPLICATE_FILM_GENRE = """
            SELECT EXISTS (
                SELECT 1
                FROM film_genre
                WHERE film_id = ? AND genre_id = ?
            ) AS record_exists;
            """;

    private static final String DELETE_LIKE = "DELETE FROM film_like  WHERE user_id = ? AND film_id = ?";
    private static final String DELETE_FILM_GENRE = "DELETE FROM film_genre WHERE film_id = ?";

    private static final String GET_MAX_ID_MPA = """
            SELECT EXISTS (
                SELECT 1
                FROM mpa
                WHERE id = ?
            ) AS id_exists;
            """;
    private static final String GET_MAX_ID_GENRE = """
            SELECT EXISTS (
                SELECT 1
                FROM genres
                WHERE id = ?
            ) AS id_exists;
            """;

    @Override
    public Collection<Film> getAll() {
        return jdbc.query(GET_ALL, rowMapper);
    }

    @Override
    public Film getFilmById(int id) {
        return jdbc.queryForObject(GET_BY_ID, rowMapper, id);
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
        return getFilmById(id);
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
        return getFilmById(id);
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
        return jdbc.query(GET_FILM_BY_LIKES, rowMapper, sizeFilms);
    }

    private Boolean heckIdMapper(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getBoolean("id_exists");
    }

    private Boolean checkDuplicateGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getBoolean("record_exists");
    }

    private void createGenre(List<Genre> genre, Integer idFilm) {
        for (Genre genres : genre) {
            validateIdGenre(genres.getId());
            if (!validateDuplicateGenre(idFilm, genres.getId())) {
                KeyHolder keyHolder = new GeneratedKeyHolder();

                jdbc.update(connection -> {
                    PreparedStatement stmt2 = connection.prepareStatement(INSERT_FILM_GENRE);
                    stmt2.setInt(1, idFilm);
                    stmt2.setInt(2, genres.getId());
                    return stmt2;
                }, keyHolder);
            }
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(birthdayFilm)) {
            String msg = "дата релиза — не раньше 28 декабря 1895 года";
            throw new ValidationException(msg);
        }
    }

    private void validateIdMpa(Integer id) {
        Boolean result = jdbc.queryForObject(GET_MAX_ID_MPA, this::heckIdMapper, id);
        if (result == null) {
            throw new NotFoundException("Ваш id = " + id + " в таблице mpa не найден");
        }
        if (!result) {
            throw new NotFoundException("Ваш id = " + id + " в таблице mpa не найден");
        }
    }

    private void validateIdGenre(Integer id) {
        Boolean result = jdbc.queryForObject(GET_MAX_ID_GENRE, this::heckIdMapper, id);
        if (result == null) {
            throw new NotFoundException("Ваш id = " + id + " в таблице genres не найден");
        }
        if (!result) {
            throw new NotFoundException("Ваш id = " + id + " в таблице genres не найден");
        }
    }

    private Boolean validateDuplicateGenre(Integer filmId, Integer genreId) {
        return jdbc.queryForObject(CHECK_DUPLICATE_FILM_GENRE, this::checkDuplicateGenre, filmId, genreId);
    }

    private void deleteGenre(Integer filmId) {
        jdbc.update(DELETE_FILM_GENRE, filmId);
    }

}
