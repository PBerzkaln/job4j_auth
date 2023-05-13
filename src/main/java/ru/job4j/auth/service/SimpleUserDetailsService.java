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

import java.util.Optional;

/**
 * Как и в Spring MVC нужно создать сервис UserDetailsService.
 * Этот сервис будет загружать в SecurityHolder детали авторизованного пользователя.
 */
@Service
@AllArgsConstructor
@ThreadSafe
public class SimpleUserDetailsService implements UserDetailsService {
    private final PersonRepository personRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Person> user = personRepository.findByLogin(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return new User(user.get().getLogin(), user.get().getPassword(), emptyList());
    }
}