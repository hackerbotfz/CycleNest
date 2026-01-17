/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cycle_nest.db;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;

public class CosmosDBManager {
    
    // Replace these with your actual keys if they are missing
    private static final String ENDPOINT = "https://freebeis.documents.azure.com:443/";
    private static final String KEY = "And31JfqYU213aNnnfdC9whpHH2jU5Kx1hT7BPghGxSvk1yl0jfqSA4SWtW4ibNr9cUH5snea8jBACDbsUhzkg==";
    private static final String DATABASE_NAME = "course-work";
    
    // Singleton Instance
    private static CosmosDBManager instance;
    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer defaultContainer;

    private CosmosDBManager() {
        try {
            this.client = new CosmosClientBuilder()
                    .endpoint(ENDPOINT)
                    .key(KEY)
                    .buildClient();
            
            this.database = client.getDatabase(DATABASE_NAME);
            // Default container (likely Items)
            this.defaultContainer = database.getContainer("sleeptype"); 
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized CosmosDBManager getInstance() {
        if (instance == null) {
            instance = new CosmosDBManager();
        }
        return instance;
    }

    // Old method (gets default container)
    public CosmosContainer getContainer() {
        return defaultContainer;
    }

    // *** NEW METHOD *** // Allows you to ask for "Requests" or "Items" specifically
    public CosmosContainer getContainer(String containerName) {
        return database.getContainer(containerName);
    }

    public void close() {
        client.close();
    }
}
