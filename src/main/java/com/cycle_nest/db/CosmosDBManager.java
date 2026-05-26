package com.cycle_nest.db;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;

public class CosmosDBManager {

    public static final String ITEMS_CONTAINER =
            env("COSMOS_ITEMS_CONTAINER", "sleeptype");
    public static final String REQUESTS_CONTAINER =
            env("COSMOS_REQUESTS_CONTAINER", "Request");

    private static final String ENDPOINT = env("COSMOS_ENDPOINT", "");
    private static final String KEY = env("COSMOS_KEY", "");
    private static final String DATABASE_NAME = env("COSMOS_DATABASE", "course-work");

    private static CosmosDBManager instance;
    private final CosmosClient client;
    private final CosmosDatabase database;
    private final CosmosContainer defaultContainer;

    private CosmosDBManager() {
        if (ENDPOINT.isBlank() || KEY.isBlank()) {
            throw new IllegalStateException(
                    "Set COSMOS_ENDPOINT and COSMOS_KEY environment variables.");
        }

        this.client = new CosmosClientBuilder()
                .endpoint(ENDPOINT)
                .key(KEY)
                .buildClient();

        this.database = client.getDatabase(DATABASE_NAME);
        this.defaultContainer = database.getContainer(ITEMS_CONTAINER);
    }

    public static synchronized CosmosDBManager getInstance() {
        if (instance == null) {
            instance = new CosmosDBManager();
        }
        return instance;
    }

    public CosmosContainer getContainer() {
        return defaultContainer;
    }

    public CosmosContainer getContainer(String containerName) {
        return database.getContainer(containerName);
    }

    public void close() {
        client.close();
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
