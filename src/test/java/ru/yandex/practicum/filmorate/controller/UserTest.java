package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    private static final Validator validator;

    static {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    private InMemoryUserStorage inMemoryUserStorage;

    @BeforeEach
    public void setUp() {
        inMemoryUserStorage = new InMemoryUserStorage();
    }

    //    электронная почта не может быть пустой и должна содержать символ @;
    @Test
    public void whenUserEmailNullOrBlankError() {
        User user = User.builder()
                .email(null)
                .login("turbo")
                .birthday(LocalDate.of(2024, 12, 12))
                .build();
//         null, Пусто
        Set<ConstraintViolation<User>> violationNullOrBlank = validator.validate(user);
        assertEquals(2, violationNullOrBlank.size());

//        Пробел (пусто)
        user.setEmail("");
        Set<ConstraintViolation<User>> violationBlank = validator.validate(user);
        assertEquals(1, violationBlank.size());

//        нет символа @
        user.setEmail("yandex.mail.ru");
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        assertEquals(1, violation.size());

//        правильный Email
        user.setEmail("yandex@mail.ru");
        Set<ConstraintViolation<User>> violationGod = validator.validate(user);
        assertEquals(0, violationGod.size());
    }

    @Test
    public void whenUserLoginNullOrBlankError() {
        User user = User.builder()
                .email("yandex@mail.ru")
                .birthday(LocalDate.of(2024, 12, 12))
                .login(null)
                .build();

//        null, Пусто
        Set<ConstraintViolation<User>> violationNullOrBlank = validator.validate(user);
        assertEquals(2, violationNullOrBlank.size());

//        Пробел (пусто)
        user.setLogin(" ");
        Set<ConstraintViolation<User>> violationBlank = validator.validate(user);
        assertEquals(1, violationBlank.size());

//        правильный Email
        user.setLogin("turbo");
        Set<ConstraintViolation<User>> violationGod = validator.validate(user);
        assertEquals(0, violationGod.size());
    }


    //    имя для отображения может быть пустым — в таком случае будет использован логин;
    @Test
    public void whenUserNameNullLoginEqualName() {
        User user = User.builder()
                .email("yandex@mail.ru")
                .login("turbo")
                .birthday(LocalDate.of(2024, 12, 12))
                .build();
        Set<ConstraintViolation<User>> violationGod = validator.validate(user);
        assertEquals(0, violationGod.size());
        User createdUser = inMemoryUserStorage.create(user);
        assertEquals(createdUser.getName(), user.getLogin());
    }

    //    дата рождения не может быть в будущем.
    @Test
    public void whenUserBirthdayFutureError() {
        User user = User.builder()
                .email("yandex@mail.ru")
                .login("turbo")
                .birthday(LocalDate.of(2025, 12, 12))
                .build();
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        assertEquals(1, violation.size());

        user.setBirthday(LocalDate.of(2024, 12, 12));
        Set<ConstraintViolation<User>> violationGod = validator.validate(user);
        assertEquals(0, violationGod.size());
    }

    //    Правильный сценарий создания
    @Test
    public void createUser() {
        User user = User.builder()
                .email("yandex@mail.ru")
                .login("turbo")
                .birthday(LocalDate.of(2024, 12, 12))
                .build();
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        assertEquals(0, violation.size());
        assertDoesNotThrow(() -> inMemoryUserStorage.create(user));
    }


    //    Правильный сценарий обновления
    @Test
    public void updateUser() {
//        сохраняем
        User user = User.builder()
                .email("yandex@mail.ru")
                .login("turbo")
                .birthday(LocalDate.of(2024, 12, 12))
                .build();
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        assertEquals(0, violation.size());
        assertDoesNotThrow(() -> inMemoryUserStorage.create(user));

//        обновляем
        user.setId(1);
        user.setLogin("turbo");
        user.setName("bobr");
        user.setEmail("bobr@mail.ru");
        user.setBirthday(LocalDate.of(2023, 12, 12));
        Set<ConstraintViolation<User>> violation2 = validator.validate(user);
        assertEquals(0, violation2.size());
        User createdUser = inMemoryUserStorage.update(user);
        assertEquals(user.toString(), createdUser.toString());


    }

}
