package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    // во всех обработчиках замените формат ответа на ErrorResponse
    // добавьте коды ошибок

//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    public Map<String, String> handleHappinessOverflow(final HappinessOverflowException e) {
//        return Map.of(
//                "error", "Осторожно, вы так избалуете питомца!",
//                "description : Текущий уровень счастья: ", e.getHappinessLevel().toString()
//        );
//    }
//
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public Map<String, String> handleIncorrectParameter(final IncorrectParameterException e) {
//        return Map.of(
//                "error", "Ошибка с входным параметром.",
//                "description", e.getMessage()
//        );
//    }
//
//    // реализуйте обработчик UnauthorizedUserException
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public Map<String, String> handleUnauthorizedUser(final UnauthorizedUserException e) {
//        return Map.of(
//                "error", "Питомец даёт себя гладить только хозяину.",
//                "description : Владелец - ", e.getOwner() + " , а пытается погладить " + e.getUser()
//        );
//    }
}