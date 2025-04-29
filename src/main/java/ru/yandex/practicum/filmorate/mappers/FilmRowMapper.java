package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final GenreDbStorage genreDbStorage;
    private final MpaRatingDbStorage mpa;

    public FilmRowMapper(GenreDbStorage genreDbStorage, MpaRatingDbStorage mpa) {
        this.genreDbStorage = genreDbStorage;
        this.mpa = mpa;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        Timestamp releaseDate = resultSet.getTimestamp("release_date");

        String genreIds = resultSet.getString("genre_ids");
        List<Genre> genres = new ArrayList<>();
        if (!genreIds.isEmpty()) {
            String[] genreIdsArray = genreIds.split(",\\s*");
            for (String id : genreIdsArray) {
                genres.add(genreDbStorage.getById(Integer.parseInt(id)));
            }
        }

        return Film.builder()
                .id(resultSet.getInt("film_id"))
                .name(resultSet.getString("film_name"))
                .description(resultSet.getString("description"))
                .releaseDate(releaseDate.toLocalDateTime().toLocalDate())
                .duration(resultSet.getInt("duration_minutes"))
                .likesCount(resultSet.getInt("likes_count"))
                .mpa(mpa.geById(resultSet.getInt("mpa_id")))
                .genres(genres)
                .build();
    }
}
