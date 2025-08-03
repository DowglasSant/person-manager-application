package com.people.manager.application.module.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${redis.db.control}")
    private int redisDbControl;

    @Value("${redis.db.buffer1}")
    private int redisDbBuffer1;

    @Value("${redis.db.buffer2}")
    private int redisDbBuffer2;

    @Bean
    public RedisConnectionFactory redisConnectionFactoryControl() {
        return createConnectionFactory(redisDbControl);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactoryBuffer1() {
        return createConnectionFactory(redisDbBuffer1);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactoryBuffer2() {
        return createConnectionFactory(redisDbBuffer2);
    }

    private LettuceConnectionFactory createConnectionFactory(int database) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        config.setPassword(RedisPassword.of(redisPassword));
        config.setDatabase(database);
        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "redisTemplateControl")
    public RedisTemplate<String, Object> redisTemplateControl() {
        return createRedisTemplate(redisConnectionFactoryControl());
    }

    @Bean(name = "redisTemplateBuffer1")
    public RedisTemplate<String, Object> redisTemplateBuffer1() {
        return createRedisTemplate(redisConnectionFactoryBuffer1());
    }

    @Bean(name = "redisTemplateBuffer2")
    public RedisTemplate<String, Object> redisTemplateBuffer2() {
        return createRedisTemplate(redisConnectionFactoryBuffer2());
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            @Qualifier("redisConnectionFactoryControl") RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
