package ru.job4j.auth.service;

import static java.util.Collections.emptyList;

import lombok.AllArgsConstructor;
import net.jcip.annotations.ThreadSafe;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.job4j.auth.model.Person;
import ru.job4j.auth.repository.PersonRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@ThreadSafe
public class SimplePersonService implements PersonService, UserDetailsService {
    private final PersonRepository personRepository;

    @Override
    public List<Person> findAll() {
        return personRepository.findAll();
    }

    @Override
    public Optional<Person> findById(int id) {
        return personRepository.findById(id);
    }

    @Override
    public Optional<Person> create(Person person) {
        personRepository.save(person);
        return Optional.of(person);
    }

    @Override
    public boolean update(Person person) {
        if (personRepository.existsById(person.getId())) {
            personRepository.save(person);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(Person person) {
        if (personRepository.existsById(person.getId())) {
            personRepository.delete(person);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Person> findByLogin(String login) {
        return personRepository.findByLogin(login);
    }

    /**
     * Как и в Spring MVC нужно создать сервис UserDetailsService.
     * Этот сервис будет загружать в SecurityHolder детали авторизованного пользователя.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Person> user = personRepository.findByLogin(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return new User(user.get().getLogin(), user.get().getPassword(), emptyList());
    }

    @Override
    public boolean partUpdate(Person person) throws InvocationTargetException, IllegalAccessException {
        var rsl = false;
        if (personRepository.existsById(person.getId())) {
            rsl = partUpdateHandler(person);
        }
        return rsl;
    }

    private boolean partUpdateHandler(Person person) throws InvocationTargetException, IllegalAccessException {
        var current = personRepository.findById(person.getId()).get();
        var methods = current.getClass().getDeclaredMethods();
        var namePerMethod = new HashMap<String, Method>();
        for (var method : methods) {
            var name = method.getName();
            if (name.startsWith("get") || name.startsWith("set")) {
                namePerMethod.put(name, method);
            }
        }
        for (var name : namePerMethod.keySet()) {
            if (name.startsWith("get")) {
                var getMethod = namePerMethod.get(name);
                var setMethod = namePerMethod.get(name.replace("get", "set"));
                if (setMethod == null) {
                    return false;
                }
                var newValue = getMethod.invoke(person);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        personRepository.save(current);
        return true;
    }
}