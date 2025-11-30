package com.people.manager.application.module.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.manager.application.module.model.Address;
import com.people.manager.application.module.model.Person;
import com.people.manager.application.module.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class RedisCacheService {
    private static final String CACHE_ACTIVE_KEY = "cache:activeDb";

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, Object> redisControlTemplate;
    private final RedisTemplate<String, Object> redisBuffer1Template;
    private final RedisTemplate<String, Object> redisBuffer2Template;
    private final PersonRepository personRepository;

    public RedisCacheService(
            ObjectMapper objectMapper,
            @Qualifier("redisTemplateControl") RedisTemplate<String, Object> redisControlTemplate,
            @Qualifier("redisTemplateBuffer1") RedisTemplate<String, Object> redisBuffer1Template,
            @Qualifier("redisTemplateBuffer2") RedisTemplate<String, Object> redisBuffer2Template,
            PersonRepository personRepository) {
        this.objectMapper = objectMapper;
        this.redisControlTemplate = redisControlTemplate;
        this.redisBuffer1Template = redisBuffer1Template;
        this.redisBuffer2Template = redisBuffer2Template;
        this.personRepository = personRepository;
    }

    private int getActiveDbNumber() {
        Object activeDbObj = redisControlTemplate.opsForValue().get(CACHE_ACTIVE_KEY);
        if (activeDbObj == null) {
            setActiveDbNumber(1);
            return 1;
        }
        return Integer.parseInt(activeDbObj.toString());
    }

    private int getInactiveDbNumber() {
        return (getActiveDbNumber() == 1) ? 2 : 1;
    }

    private void setActiveDbNumber(int dbNumber) {
        redisControlTemplate.opsForValue().set(CACHE_ACTIVE_KEY, String.valueOf(dbNumber));
        log.info("Switched active Redis DB to DB{}", dbNumber);
    }

    private RedisTemplate<String, Object> getActiveRedisTemplate() {
        return (getActiveDbNumber() == 1) ? redisBuffer1Template : redisBuffer2Template;
    }

    private RedisTemplate<String, Object> getInactiveRedisTemplate() {
        return (getInactiveDbNumber() == 1) ? redisBuffer1Template : redisBuffer2Template;
    }

    @Scheduled(cron = "${cache.cron.expression}")
    public void scheduledCacheJob() {
        log.info("Starting cache job...");

        RedisTemplate<String, Object> inactiveTemplate = getInactiveRedisTemplate();
        int inactiveDb = getInactiveDbNumber();
        log.info("Inactive Redis DB selected: DB{}", inactiveDb);

        clearPersonAndIndexKeys(inactiveTemplate);
        cachePeopleInBatches(inactiveTemplate);
        setActiveDbNumber(inactiveDb);

        log.info("Cache job finished. Active DB is now DB{}", inactiveDb);
    }

    private void clearPersonAndIndexKeys(RedisTemplate<String, Object> template) {
        log.info("Clearing old cache keys for people and indexes...");

        assert template.getConnectionFactory() != null;

        try (RedisConnection connection = template.getConnectionFactory().getConnection()) {
            ScanOptions scanOptions = ScanOptions.scanOptions().match("person_*").build();
            Cursor<byte[]> cursor = connection.scan(scanOptions);
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                connection.keyCommands().del(key);
            }

            scanOptions = ScanOptions.scanOptions().match("people_by_*").build();
            cursor = connection.scan(scanOptions);
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                connection.keyCommands().del(key);
            }

            scanOptions = ScanOptions.scanOptions().match("person_by_cpf_*").build();
            cursor = connection.scan(scanOptions);
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                connection.keyCommands().del(key);
            }

            log.info("Old cache keys cleared.");

        }
    }

    private void cachePeopleInBatches(RedisTemplate<String, Object> template) {
        log.info("Fetching all people from database...");
        List<Person> allPeople = personRepository.findAll();

        log.info("Total people fetched: {}", allPeople.size());

        List<List<Person>> batches = IntStream.range(0, (allPeople.size() + 99) / 100)
                .mapToObj(i -> allPeople.subList(i * 100, Math.min((i + 1) * 100, allPeople.size())))
                .toList();

        log.info("Total batches: {}", batches.size());

        ForkJoinPool customPool = new ForkJoinPool(10);

        try {
            customPool.submit(() ->
                    batches.parallelStream().forEach(batch -> {
                        assert template.getConnectionFactory() != null;
                        RedisConnection connection = template.getConnectionFactory().getConnection();
                        connection.openPipeline();
                        try {
                            for (Person person : batch) {
                                byte[] personJson;
                                try {
                                    personJson = objectMapper.writeValueAsBytes(person);
                                } catch (JsonProcessingException e) {
                                    log.error("Failed to serialize person {}", person.getId(), e);
                                    continue;
                                }

                                String personKey = "person_" + person.getId() + "_" + person.getCpf();
                                connection.stringCommands().set(
                                        personKey.getBytes(StandardCharsets.UTF_8),
                                        personJson
                                );

                                connection.setCommands().sAdd(
                                        "all_people".getBytes(StandardCharsets.UTF_8),
                                        personKey.getBytes(StandardCharsets.UTF_8)
                                );

                                Address addr = person.getAddress();
                                if (addr != null) {
                                    if (addr.getCity() != null) {
                                        String key = "people_by_city_" + addr.getCity();
                                        connection.setCommands().sAdd(key.getBytes(), personKey.getBytes());
                                    }
                                    if (addr.getState() != null) {
                                        String key = "people_by_state_" + addr.getState();
                                        connection.setCommands().sAdd(key.getBytes(), personKey.getBytes());
                                    }
                                    if (addr.getCountry() != null) {
                                        String key = "people_by_country_" + addr.getCountry();
                                        connection.setCommands().sAdd(key.getBytes(), personKey.getBytes());
                                    }
                                }

                                String cpfKey = "person_by_cpf_" + person.getCpf();
                                connection.setCommands().sAdd(cpfKey.getBytes(), personKey.getBytes());
                            }
                        } finally {
                            connection.closePipeline();
                            connection.close();
                        }
                    })
            ).get();
        } catch (Exception e) {
            log.error("Error during cache batch processing", e);
        } finally {
            customPool.shutdown();
        }

        log.info("All people cached successfully in inactive DB.");
    }

    public Set<String> getIndexMembers(String indexName) {
        RedisTemplate<String, Object> template = getActiveRedisTemplate();
        RedisConnection connection = Objects.requireNonNull(template.getConnectionFactory()).getConnection();
        try {
            Set<byte[]> members = connection.setCommands()
                    .sMembers(indexName.getBytes(StandardCharsets.UTF_8));
            if (members == null || members.isEmpty()) {
                return Collections.emptySet();
            }
            return members.stream()
                    .map(b -> new String(b, StandardCharsets.UTF_8))
                    .collect(Collectors.toSet());
        } finally {
            connection.close();
        }
    }

    public List<Person> getPeopleByKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        RedisTemplate<String, Object> template = getActiveRedisTemplate();
        RedisConnection connection = Objects.requireNonNull(template.getConnectionFactory()).getConnection();
        List<Person> people = new ArrayList<>();

        try {
            connection.openPipeline();

            List<byte[]> byteKeys = keys.stream()
                    .map(k -> k.getBytes(StandardCharsets.UTF_8))
                    .toList();

            connection.stringCommands().mGet(byteKeys.toArray(new byte[0][]));

            List<Object> pipelineResults = connection.closePipeline();

            if (!pipelineResults.isEmpty() && pipelineResults.get(0) instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<byte[]> rawResults = (List<byte[]>) pipelineResults.get(0);

                for (byte[] data : rawResults) {
                    if (data != null) {
                        try {
                            Person person = objectMapper.readValue(data, Person.class);
                            people.add(person);
                        } catch (Exception e) {
                            log.error("Failed to deserialize person from Redis", e);
                        }
                    }
                }
            }
        } finally {
            connection.close();
        }

        return people;
    }
}
