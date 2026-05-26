package com.cycle_nest.queue;

import com.azure.cosmos.CosmosContainer;
import com.cycle_nest.db.CosmosDBManager;
import com.cycle_nest.model.RentalRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class RabbitMQWorker {

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = RabbitMQConfig.newConnectionFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RabbitMQConfig.QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received: " + message);

            try {
                ObjectMapper mapper = new ObjectMapper();
                RentalRequest request = mapper.readValue(message, RentalRequest.class);
                CosmosContainer container = CosmosDBManager.getInstance()
                        .getContainer(CosmosDBManager.REQUESTS_CONTAINER);
                container.createItem(request);
                System.out.println(" [✓] Saved request " + request.getId());
            } catch (Exception e) {
                System.err.println(" [x] Failed to process message: " + e.getMessage());
                e.printStackTrace();
            }
        };

        channel.basicConsume(RabbitMQConfig.QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
