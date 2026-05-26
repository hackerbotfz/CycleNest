package com.cycle_nest.queue;

import com.rabbitmq.client.ConnectionFactory;

public final class RabbitMQConfig {

    public static final String QUEUE_NAME = "rental_requests_queue";

    private RabbitMQConfig() {
    }

    public static ConnectionFactory newConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(env("RABBITMQ_HOST", "localhost"));
        String user = System.getenv("RABBITMQ_USER");
        String password = System.getenv("RABBITMQ_PASSWORD");
        if (user != null && !user.isBlank()) {
            factory.setUsername(user);
        }
        if (password != null && !password.isBlank()) {
            factory.setPassword(password);
        }
        return factory;
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
