package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
public class MpaRatingDbStorage {
    protected final JdbcTemplate jdbc;

    public MpaRatingDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String GET_BY_ID = "SELECT * FROM mpa WHERE id = ?;";
    private static final String CHECK_ID = "SELECT id FROM mpa WHERE id = ?;";
    private static final String GET_ALL = "SELECT * FROM mpa ORDER BY  id ;";

    public MpaRating getNameById(Integer id) {
        validateMpaId(id);
        return jdbc.queryForObject(GET_BY_ID, MpaRatingDbStorage::getMpaRatingMapper, id);
    }

    public Collection<MpaRating> getAll() {
        return jdbc.query(GET_ALL, MpaRatingDbStorage::getMpaRatingMapper);
    }


    private static MpaRating getMpaRatingMapper(ResultSet resultSet, int rowNum) throws SQLException {
        return MpaRating.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("title"))
                .build();
    }

    private void validateMpaId(Integer id) {
        List<Integer> ids = jdbc.queryForList(CHECK_ID, Integer.class, id);
        if (ids.isEmpty()) {
            throw new NotFoundException("id = " + id + " не найден");
        }
    }

}
