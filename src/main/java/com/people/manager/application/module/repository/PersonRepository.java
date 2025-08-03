package com.people.manager.application.module.repository;

import com.people.manager.application.module.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByCpf(String cpf);

    List<Person> findByAddressCity(String city);

    List<Person> findByAddressState(String state);

    List<Person> findByAddressCountry(String country);
}
