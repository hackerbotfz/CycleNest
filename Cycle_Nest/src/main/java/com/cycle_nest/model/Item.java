/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cycle_nest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// @JsonIgnoreProperties ignores fields in the DB that aren't in this class (prevents crashes)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    
    // 'id' is required by Cosmos DB
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("ownerId")
    private String ownerId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("category")
    private String category;
    
    // We store location as a generic string (Postcode or "lat,lon")
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("dailyRate")
    private double dailyRate;
    
    @JsonProperty("availability")
    private boolean availability;
    
    @JsonProperty("condition")
    private String condition; // e.g., "Good", "Like New"
    
    @JsonProperty("description")
    private String description;

    // Default Constructor (Required by Jackson for JSON deserialization)
    public Item() {}

    // Convenience Constructor
    public Item(String id, String ownerId, String name, String category, double dailyRate) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.category = category;
        this.dailyRate = dailyRate;
        this.availability = true;
    }

    // --- Getters and Setters (Essential for Jackson) ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    public boolean isAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
