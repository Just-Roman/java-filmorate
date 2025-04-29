package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaRatingController {
    private final MpaRatingService mpaRatingService;

    @GetMapping("/{id}")
    public MpaRating getById(@PathVariable Integer id) {
        return mpaRatingService.getById(id);
    }

    @GetMapping
    public Collection<MpaRating> getAll() {
        return mpaRatingService.getAll();
    }

}
