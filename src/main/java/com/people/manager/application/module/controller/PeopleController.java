package com.people.manager.application.module.controller;

import com.people.manager.application.module.model.Person;
import com.people.manager.application.module.service.impl.PeopleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/people")
public class PeopleController {

    private final PeopleService peopleService;

    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping
    public ResponseEntity<List<Person>> getAllPeople() {
        List<Person> people = peopleService.getAllPeople();
        return ResponseEntity.ok(people);
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Person> getPersonByCpf(@PathVariable String cpf) {
        Person person = peopleService.getPersonByCpf(cpf);
        if (person == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(person);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Person>> getPeopleByCity(@PathVariable String city) {
        List<Person> people = peopleService.getPeopleByCity(city);
        return ResponseEntity.ok(people);
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<List<Person>> getPeopleByState(@PathVariable String state) {
        List<Person> people = peopleService.getPeopleByState(state);
        return ResponseEntity.ok(people);
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<Person>> getPeopleByCountry(@PathVariable String country) {
        List<Person> people = peopleService.getPeopleByCountry(country);
        return ResponseEntity.ok(people);
    }
}
