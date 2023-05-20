package ru.job4j.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.auth.dto.PersonDTO;
import ru.job4j.auth.model.Person;
import ru.job4j.auth.service.PersonService;
import ru.job4j.auth.util.Operation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
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
    private static final Logger LOG = LogManager.getLogger(PersonController.class.getName());
    private final PersonService personService;
    private final BCryptPasswordEncoder encoder;
    private final ObjectMapper objectMapper;

    @GetMapping("/all")
    public List<Person> findAll() {
        return personService.findAll();
    }

    /**
     * Если не нужно как-то отслеживать исключения приложения,
     * прописывать сложную логику, указывать специфические детали об ошибках и т.п.,
     * можно просто пробрасывать исключение ResponseStatusException.
     *
     * <br>ResponseStatusException в конструкторе принимает HTTP статус и сообщение,
     * которое нужно вывести пользователю.
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = personService.findById(id);
        if (person.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Person is not found. Please, check requisites.");
        }
        return new ResponseEntity<>(person.get(), HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> create(@Validated(Operation.OnCreate.class)
                                         @RequestBody Person person) {
        /**
         * Пароли хешируются и прямом виде не хранятся в базе.
         */
        person.setPassword(encoder.encode(person.getPassword()));
        return new ResponseEntity<>(personService.create(person).get(), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<String> update(@Validated(Operation.OnUpdate.class)
                                         @RequestBody Person person) {
        person.setPassword(encoder.encode(person.getPassword()));
        var rsl = personService.update(person);
        if (!rsl) {
            return ResponseEntity.badRequest()
                    .body("Не удалось обновить данные");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@Validated(Operation.OnDelete.class)
                                         @PathVariable int id) {
        Person person = new Person();
        person.setId(id);
        var rsl = personService.delete(person);
        if (!rsl) {
            return ResponseEntity.badRequest()
                    .body("Не удалось удалить данные");
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/partUpdate")
    public ResponseEntity<String> partUpdate(@Validated(Operation.OnPartUpdate.class)
                                             @RequestBody PersonDTO personDTO) {
        personDTO.setPassword(encoder.encode(personDTO.getPassword()));
        var rsl = personService.partUpdate(personDTO);
        if (!rsl) {
            return ResponseEntity.badRequest()
                    .body("Не удалось частично обновить данные");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Данная аннотация позволяет отслеживать
     * и обрабатывать исключения на уровне класса.
     * Если использовать ее например в контроллере,
     * то исключения только данного контроллера будут обрабатываться.
     *
     * <br>value = { IllegalArgumentException.class } указывает,
     * что обработчик будет обрабатывать только данное исключение.
     * Можно перечислить их больше, т.к. value это массив.
     *
     * <br>Метод, помеченный как @ExceptionHandler,
     * поддерживает внедрение аргументов и возвращаемого типа в рантайме,
     * указанных в спецификации. По этому мы можем внедрить запрос,
     * ответ и само исключение, чтобы прописать какую-либо логику.
     *
     * <br>В данном случае при возникновении исключения IllegalArgumentException,
     * метод exceptionHandler() отлавливает его и меняет ответ,
     * а именно его статус и тело. Также в последней строке происходит логгирование.
     *
     * @param e
     * @param request
     * @param response
     * @throws IOException
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    public void exceptionHandler(Exception e, HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() {
            {
                put("message", e.getMessage());
                put("type", e.getClass());
            }
        }));
        LOG.error(e.getLocalizedMessage());
    }
}