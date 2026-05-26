package com.cycle_nest.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class RabbitMQProducer {

    private static final ConnectionFactory factory = RabbitMQConfig.newConnectionFactory();

    public void sendRequest(String jsonMessage) {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(RabbitMQConfig.QUEUE_NAME, true, false, false, null);
            channel.basicPublish("", RabbitMQConfig.QUEUE_NAME, null,
                    jsonMessage.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent to Queue: '" + jsonMessage + "'");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send message to RabbitMQ");
        }
    }
}
