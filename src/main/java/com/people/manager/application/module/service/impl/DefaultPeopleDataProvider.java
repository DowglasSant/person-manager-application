package com.people.manager.application.module.service.impl;

import com.people.manager.application.module.model.Person;
import com.people.manager.application.module.repository.PersonRepository;
import com.people.manager.application.module.service.PeopleDataProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultPeopleDataProvider implements PeopleDataProvider {

    private final PersonRepository repository;

    public DefaultPeopleDataProvider(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Person> findAll() {
        return repository.findAll();
    }

    @Override
    public Person findByCpf(String cpf) {
        return repository.findByCpf(cpf).orElse(null);
    }

    @Override
    public List<Person> findByCity(String city) {
        return repository.findByAddressCity(city);
    }

    @Override
    public List<Person> findByState(String state) {
        return repository.findByAddressState(state);
    }

    @Override
    public List<Person> findByCountry(String country) {
        return repository.findByAddressCountry(country);
    }
}
