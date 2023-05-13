package ru.job4j.auth.service;

import ru.job4j.auth.model.Person;

import java.util.List;
import java.util.Optional;

public interface PersonService {
    List<Person> findAll();

    Optional<Person> findById(int id);

    Optional<Person> create(Person person);

    boolean update(Person person);

    boolean delete(Person person);
    Optional<Person> findByLogin(String login);
}