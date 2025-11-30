package com.people.manager.application.module.infra;

public final class CacheKeys {

    public static final String ALL = "all_people";

    public static String byCpf(String cpf) {
        return "person_by_cpf_" + cpf;
    }

    public static String byCity(String city) {
        return "people_by_city_" + city;
    }

    public static String byState(String state) {
        return "people_by_state_" + state;
    }

    public static String byCountry(String country) {
        return "people_by_country_" + country;
    }

    public static String personKey(Long id, String cpf) {
        return "person_" + id + "_" + cpf;
    }
}

