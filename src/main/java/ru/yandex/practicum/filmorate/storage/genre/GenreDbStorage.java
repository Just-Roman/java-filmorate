package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
public class GenreDbStorage {
    protected final JdbcTemplate jdbc;

    public GenreDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String GET_BY_ID = "SELECT * FROM genres WHERE id = ?;";
    private static final String CHECK_ID = "SELECT id FROM genres WHERE id = ?;";
    private static final String GET_ALL = "SELECT * FROM genres ORDER BY id ;";

    public Genre getById(Integer id) {
        validateId(id);
        return jdbc.queryForObject(GET_BY_ID, this::getGenreMapper, id);
    }

    public Collection<Genre> getAll() {
        return jdbc.query(GET_ALL, this::getGenreMapper);
    }

    private Genre getGenreMapper(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("title"))
                .build();
    }

    private void validateId(Integer id) {
        List<Integer> ids = jdbc.queryForList(CHECK_ID, Integer.class, id);
        if (ids.isEmpty()) {
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
    }

}
