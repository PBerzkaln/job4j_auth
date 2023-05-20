package ru.job4j.auth.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.job4j.auth.util.Operation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull @Min(value = 1, message = "Id must be non null", groups = {
            Operation.OnUpdate.class, Operation.OnDelete.class})
    private int id;

    @EqualsAndHashCode.Include
    @NotBlank(message = "Login must be not empty", groups = {
            Operation.OnCreate.class, Operation.OnUpdate.class})
    private String login;

    @EqualsAndHashCode.Include
    @NotBlank(message = "Password must be not empty", groups = {
            Operation.OnCreate.class, Operation.OnUpdate.class})
    private String password;
}