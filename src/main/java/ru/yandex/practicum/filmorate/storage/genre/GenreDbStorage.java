package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GenreDbStorage {
    protected final JdbcTemplate jdbc;


    public GenreDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
