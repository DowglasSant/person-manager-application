package com.people.manager.application.module.service;

import com.people.manager.application.module.model.Person;
import com.people.manager.application.module.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class PeopleService {

    private final PersonRepository personRepository;
    private final RedisCacheService redisCacheService;

    public PeopleService(PersonRepository personRepository, RedisCacheService redisCacheService) {
        this.personRepository = personRepository;
        this.redisCacheService = redisCacheService;
    }

    public List<Person> getAllPeople() {
        try {
            Set<String> keys = redisCacheService.getIndexMembers("all_people");

            if (keys.isEmpty()) {
                log.info("Redis cache miss for all people, querying DB.");
                return personRepository.findAll();
            }

            return redisCacheService.getPeopleByKeys(keys);
        } catch (Exception e) {
            log.error("Redis error on getAllPeople(), returning empty list.", e);
            return Collections.emptyList();
        }
    }

    public Person getPersonByCpf(String cpf) {
        String indexName = "person_by_cpf_" + cpf;

        try {
            Set<String> keys = redisCacheService.getIndexMembers(indexName);

            if (keys.isEmpty()) {
                log.info("Redis cache miss for CPF {}, querying DB.", cpf);
                return personRepository.findByCpf(cpf).orElse(null);
            }

            List<Person> people = redisCacheService.getPeopleByKeys(keys);
            if (people.isEmpty()) {
                return null;
            }

            return people.get(0);
        } catch (Exception e) {
            log.error("Redis error on getPersonByCpf({}), returning null.", cpf, e);
            return null;
        }
    }

    public List<Person> getPeopleByCity(String city) {
        String indexName = "people_by_city_" + city;

        try {
            Set<String> keys = redisCacheService.getIndexMembers(indexName);

            if (keys.isEmpty()) {
                log.info("Redis cache miss for city {}, querying DB.", city);
                return personRepository.findByAddressCity(city);
            }

            return redisCacheService.getPeopleByKeys(keys);
        } catch (Exception e) {
            log.error("Redis error on getPeopleByCity({}), returning empty list.", city, e);
            return Collections.emptyList();
        }
    }

    public List<Person> getPeopleByState(String state) {
        String indexName = "people_by_state_" + state;
        try {
            Set<String> keys = redisCacheService.getIndexMembers(indexName);

            if (keys.isEmpty()) {
                log.info("Redis cache miss for state {}, querying DB.", state);
                return personRepository.findByAddressState(state);
            }

            return redisCacheService.getPeopleByKeys(keys);
        } catch (Exception e) {
            log.error("Redis error on getPeopleByState({}), returning empty list.", state, e);
            return Collections.emptyList();
        }
    }

    public List<Person> getPeopleByCountry(String country) {
        String indexName = "people_by_country_" + country;

        try {
            Set<String> keys = redisCacheService.getIndexMembers(indexName);

            if (keys.isEmpty()) {
                log.info("Redis cache miss for country {}, querying DB.", country);
                return personRepository.findByAddressCountry(country);
            }

            return redisCacheService.getPeopleByKeys(keys);
        } catch (Exception e) {
            log.error("Redis error on getPeopleByCountry({}), returning empty list.", country, e);
            return Collections.emptyList();
        }
    }
}
