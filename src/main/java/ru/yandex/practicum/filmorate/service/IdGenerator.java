package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;

@Service
public class IdGenerator {
    private int id = 1;

    public int getNextId() {
        return id++;
    }
}
