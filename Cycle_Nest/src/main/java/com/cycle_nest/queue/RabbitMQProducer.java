/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cycle_nest.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
/**
 *
 * @author ShadowSCI
 */
public class RabbitMQProducer {

    private final static String QUEUE_NAME = "rental_requests_queue";
    private static ConnectionFactory factory;
    
    // Singleton connection factory setup
    static {
        factory = new ConnectionFactory();
        factory.setHost("localhost"); // Change to your RabbitMQ IP if not local
        // factory.setUsername("guest"); // Default is guest/guest
        // factory.setPassword("guest");
    }

    public void sendRequest(String jsonMessage) {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            
            // Declare queue (idempotent - safe to run multiple times)
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            
            // Publish message
            channel.basicPublish("", QUEUE_NAME, null, jsonMessage.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent to Queue: '" + jsonMessage + "'");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send message to RabbitMQ");
        }
    }
}
