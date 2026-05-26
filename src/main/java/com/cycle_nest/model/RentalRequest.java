/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cycle_nest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class RentalRequest {
    
    @JsonProperty("id")
    private String id; // Unique Request ID
    
    @JsonProperty("itemId")
    private String itemId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("status")
    private String status; // pending, cancelled, confirmed
    
    // Partition Key for Cosmos DB (we group requests by userId)
    @JsonProperty("type")
    private String type = "request"; 

    public RentalRequest() {
        // Auto-generate ID if not provided
        this.id = UUID.randomUUID().toString();
        this.status = "pending";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
