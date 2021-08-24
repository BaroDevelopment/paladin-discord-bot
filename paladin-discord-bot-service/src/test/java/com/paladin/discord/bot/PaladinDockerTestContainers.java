package com.paladin.discord.bot;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.Map;
import java.util.stream.Stream;

@ContextConfiguration(initializers = PaladinDockerTestContainers.Initializer.class)
abstract class PaladinDockerTestContainers {

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:9.6.22-alpine3.14")
                .withDatabaseName("Paladin");
//                .withReuse(true);

        static GenericContainer<?> redis = new GenericContainer<>("redis:3-alpine")
                .withExposedPorts(6379);
//                .withReuse(true);

        public static Map<String, String> getProperties() {
            Startables.deepStart(Stream.of(redis, postgres)).join();

            return Map.of(
                    "spring.datasource.url", postgres.getJdbcUrl(),
                    "spring.datasource.username", postgres.getUsername(),
                    "spring.datasource.password", postgres.getPassword(),
                    "spring.redis.host", redis.getContainerIpAddress(),
                    "spring.redis.port", redis.getFirstMappedPort() + ""
            );
        }

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            var env = context.getEnvironment();
            env.getPropertySources().addFirst(new MapPropertySource(
                    "testcontainers",
                    (Map) getProperties()
            ));
        }
    }
}
