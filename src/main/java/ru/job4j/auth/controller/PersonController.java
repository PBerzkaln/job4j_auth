package ru.job4j.auth.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.job4j.auth.model.Person;
import ru.job4j.auth.service.PersonService;

import java.util.List;

/**
 * Этот контроллер описывает CRUD операции и построен по схеме Rest архитектуры:
 * <br>GET/person/ список всех пользователей.
 * <br>GET/person/{id} - пользователь с id.
 * <br>POST/person/ - создает пользователя.
 * <br>PUT/person/ - обновляет пользователя.
 * <br>DELETE/person/ - удаляет.
 */
@RestController
@RequestMapping("/person")
@AllArgsConstructor
public class PersonController {
    private final PersonService personService;

    @GetMapping("/")
    public List<Person> findAll() {
        return personService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = personService.findById(id);
        return new ResponseEntity<>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        return new ResponseEntity<>(
                personService.create(person).get(),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<String> update(@RequestBody Person person) {
        var rsl = personService.update(person);
        if (!rsl) {
            return ResponseEntity.badRequest()
                    .body("Не удалось обновить данные");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable int id) {
        Person person = new Person();
        person.setId(id);
        var rsl = personService.delete(person);
        if (!rsl) {
            return ResponseEntity.badRequest()
                    .body("Не удалось удалить данные");
        }
        return ResponseEntity.ok().build();
    }
}