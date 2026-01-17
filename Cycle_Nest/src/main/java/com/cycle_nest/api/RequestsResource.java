/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cycle_nest.api;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.cycle_nest.db.CosmosDBManager;
import com.cycle_nest.model.RentalRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
/**
 *
 * @author ShadowSCI
 */
@Path("requests") // Replaces old root path
public class RequestsResource {

    // Explicit connection to "Requests" container (Used only for cancellation/updates)
    private final CosmosContainer requestsContainer = CosmosDBManager.getInstance().getContainer("Request");
    
    // RabbitMQ Config
    private final static String QUEUE_NAME = "rental_requests_queue";

    // POST /api/requests
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRequest(RentalRequest request) {
        if (request == null || request.getItemId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Payload").build();
        }

        try {
            request.setStatus("queued");

            // Convert to JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(request);

            // Send to RabbitMQ
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                channel.basicPublish("", QUEUE_NAME, null, jsonString.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent to Queue: '" + jsonString + "'");
            }

            return Response.status(Response.Status.ACCEPTED)
                    .entity("{\"message\":\"Request queued\", \"id\":\""+request.getId()+"\"}")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Failed to queue request").build();
        }
    }

    // PUT /api/requests/{id}/cancel
    @PUT
    @Path("{id}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelRequest(@PathParam("id") String id) {
        try {
            // 1. Find the request in the Requests Container
            String query = "SELECT * FROM c WHERE c.id = '" + id + "'";
            CosmosPagedIterable<RentalRequest> requests = requestsContainer.queryItems(query, new CosmosQueryRequestOptions(), RentalRequest.class);
            
            RentalRequest requestToUpdate = requests.iterator().hasNext() ? requests.iterator().next() : null;

            if (requestToUpdate == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // 2. Update status
            requestToUpdate.setStatus("cancelled");
            
            // 3. Save back to Requests Container
            requestsContainer.upsertItem(requestToUpdate);

            return Response.ok(requestToUpdate).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }
}
