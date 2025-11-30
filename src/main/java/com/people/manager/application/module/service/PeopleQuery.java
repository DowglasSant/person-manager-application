package com.people.manager.application.module.service;

import com.people.manager.application.module.model.Person;

import java.util.List;

public interface PeopleQuery {
    List<Person> findAll();
    Person findByCpf(String cpf);
    List<Person> findByCity(String city);
    List<Person> findByState(String state);
    List<Person> findByCountry(String country);
}
