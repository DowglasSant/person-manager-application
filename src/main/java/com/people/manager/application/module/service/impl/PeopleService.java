package com.people.manager.application.module.service.impl;

import com.people.manager.application.module.model.Person;
import com.people.manager.application.module.service.PeopleQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeopleService {

    private final PeopleQuery query;

    public PeopleService(PeopleQuery query) {
        this.query = query;
    }

    public List<Person> getAllPeople() {
        return query.findAll();
    }

    public Person getPersonByCpf(String cpf) {
        return query.findByCpf(cpf);
    }

    public List<Person> getPeopleByCity(String city) {
        return query.findByCity(city);
    }

    public List<Person> getPeopleByState(String state) {
        return query.findByState(state);
    }

    public List<Person> getPeopleByCountry(String country) {
        return query.findByCountry(country);
    }
}

