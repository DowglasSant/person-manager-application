package com.people.manager.application.module.service.impl;

import com.people.manager.application.module.infra.CacheKeys;
import com.people.manager.application.module.model.Person;
import com.people.manager.application.module.service.PeopleDataProvider;
import com.people.manager.application.module.service.PeopleQuery;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Primary
@Service
public class RedisPeopleQuery implements PeopleQuery {

    private final RedisCacheService redis;
    private final PeopleDataProvider fallback;

    public RedisPeopleQuery(RedisCacheService redis,
                            PeopleDataProvider fallback) {
        this.redis = redis;
        this.fallback = fallback;
    }

    @Override
    public List<Person> findAll() {
        var keys = redis.getIndexMembers(CacheKeys.ALL);
        return keys.isEmpty() ? fallback.findAll() : redis.getPeopleByKeys(keys);
    }

    @Override
    public Person findByCpf(String cpf) {
        var keys = redis.getIndexMembers(CacheKeys.byCpf(cpf));
        if (keys.isEmpty()) return fallback.findByCpf(cpf);
        return redis.getPeopleByKeys(keys).stream().findFirst().orElse(null);
    }

    @Override
    public List<Person> findByCity(String city) {
        var keys = redis.getIndexMembers(CacheKeys.byCity(city));
        return keys.isEmpty() ? fallback.findByCity(city)
                : redis.getPeopleByKeys(keys);
    }

    @Override
    public List<Person> findByState(String state) {
        var keys = redis.getIndexMembers(CacheKeys.byState(state));
        return keys.isEmpty() ? fallback.findByState(state)
                : redis.getPeopleByKeys(keys);
    }

    @Override
    public List<Person> findByCountry(String country) {
        var keys = redis.getIndexMembers(CacheKeys.byCountry(country));
        return keys.isEmpty() ? fallback.findByCountry(country)
                : redis.getPeopleByKeys(keys);
    }
}


