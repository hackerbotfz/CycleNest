/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cycle_nest.queue;

import com.azure.cosmos.CosmosContainer;
import com.cycle_nest.db.CosmosDBManager;
import com.cycle_nest.model.RentalRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
/**
 *
 * @author ShadowSCI
 */
public class RabbitMQWorker {

    private final static String QUEUE_NAME = "rental_requests_queue";

    public static void main(String[] argv) throws Exception {
        // 1. Setup Connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 2. Define the callback
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("\n========================================");
            System.out.println(" [x] RECEIVED RAW MESSAGE: " + message);

            try {
                // A. Convert JSON to Java Object
                ObjectMapper mapper = new ObjectMapper();
                RentalRequest request = mapper.readValue(message, RentalRequest.class);
                System.out.println(" [x] Parsed ID: " + request.getId());
                System.out.println(" [x] Parsed UserID: " + request.getUserId());

                // B. Connect to Azure
                System.out.println(" [?] Connecting to Cosmos DB 'Request' container...");
                // *** CHECK THIS LINE: Does your Azure container start with Capital 'R'? ***
                CosmosContainer container = CosmosDBManager.getInstance().getContainer("Request"); 

                // C. Save to Azure
                System.out.println(" [?] Attempting to save...");
                container.createItem(request);
                
                System.out.println(" [✓] SUCCESS! SAVED TO AZURE.");
                System.out.println("========================================\n");

            } catch (Exception e) {
                System.err.println(" [!!!!!] CRITICAL ERROR [!!!!!] ");
                System.err.println("Error Type: " + e.getClass().getName());
                System.err.println("Error Message: " + e.getMessage());
                
                // Print the 'Cause' if it exists (often hides the real reason)
                if (e.getCause() != null) {
                    System.err.println("Real Cause: " + e.getCause().getMessage());
                }
                
                System.err.println("========================================");
                e.printStackTrace();
            }
        };

        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}