/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cycle_nest.api;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.cycle_nest.db.CosmosDBManager;
import com.cycle_nest.model.Item;
import com.cycle_nest.service.DistanceService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author ShadowSCI
 */
@Path("items") // Replaces the old root path
public class ItemsResource {

    // explicit connection to your Inventory container
    private final CosmosContainer container = CosmosDBManager.getInstance().getContainer("sleeptype");
    private final DistanceService distanceService = new DistanceService();

    // GET /api/items
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItems(
            @QueryParam("category") String category,
            @QueryParam("maxPrice") Double maxPrice) {

        try {
            // Build SQL Query
            StringBuilder sql = new StringBuilder("SELECT * FROM c WHERE c.availability = true");

            // Filters
            if (category != null && !category.isEmpty()) {
                sql.append(" AND c.category = '").append(category).append("'");
            }
            if (maxPrice != null) {
                sql.append(" AND c.dailyRate <= ").append(maxPrice);
            }

            // Execute Query
            CosmosPagedIterable<Item> items = container.queryItems(
                    sql.toString(), 
                    new CosmosQueryRequestOptions(), 
                    Item.class
            );

            List<Item> resultList = new ArrayList<>();
            items.forEach(resultList::add);

            return Response.ok(resultList).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Database query failed\"}").build();
        }
    }

    // GET /api/items/{id}/proximity
    @GET
    @Path("{id}/proximity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProximity(
            @PathParam("id") String itemId,
            @QueryParam("userLat") String userLat,
            @QueryParam("userLon") String userLon) {

        if (userLat == null || userLon == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing userLat or userLon\"}").build();
        }

        try {
            // 1. Get Item from DB
            String query = "SELECT * FROM c WHERE c.id = '" + itemId + "'";
            CosmosPagedIterable<Item> items = container.queryItems(query, new CosmosQueryRequestOptions(), Item.class);
            
            if (!items.iterator().hasNext()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Item not found").build();
            }
            Item item = items.iterator().next();

            // 2. Parse Location ("lat,lon")
            String[] itemLoc = item.getLocation().split(",");
            if (itemLoc.length != 2) {
                 return Response.serverError().entity("Invalid item location format in DB").build();
            }
            
            // 3. Calculate Distance
            double distance = distanceService.getDistanceKm(userLat, userLon, itemLoc[0].trim(), itemLoc[1].trim());

            if (distance < 0) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity("{\"error\":\"Could not calculate distance\"}").build();
            }

            return Response.ok("{\"distance_km\": " + distance + "}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }
}
