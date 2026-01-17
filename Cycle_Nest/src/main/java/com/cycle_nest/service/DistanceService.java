/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cycle_nest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DistanceService {

    // OSRM Public API Endpoint
    // NOTE: For a real production app, you would host your own OSRM instance.
    // The public demo server has rate limits.
    private static final String OSRM_API_URL = "http://router.project-osrm.org/route/v1/driving/";

    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;

    public DistanceService() {
        // Set timeouts to prevent the server from hanging if OSRM is slow
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
    }

    /**
     * Calculates driving distance between two points.
     * * @param originLatStr      User's Latitude
     * @param originLonStr      User's Longitude
     * @param destLatStr        Item's Latitude
     * @param destLonStr        Item's Longitude
     * @return Distance in Kilometers, or -1.0 if failed.
     */
    public double getDistanceKm(String originLatStr, String originLonStr, String destLatStr, String destLonStr) {
        try {
            // 1. Validate Inputs
            if (originLatStr == null || destLatStr == null) return -1.0;

            // 2. Format URL: OSRM requires "lon,lat" (Longitude FIRST)
            // Format: {lon1},{lat1};{lon2},{lat2}
            String coordinates = originLonStr + "," + originLatStr + ";" + destLonStr + "," + destLatStr;
            String requestUrl = OSRM_API_URL + coordinates + "?overview=false";

            // 3. Build Request
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .build();

            // 4. Execute Call
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("OSRM API Error: " + response.code());
                    return -1.0;
                }

                // 5. Parse JSON
                String jsonResponse = response.body().string();
                JsonNode rootNode = mapper.readTree(jsonResponse);

                // Navigate: root -> routes -> [0] -> distance
                if (rootNode.has("routes") && rootNode.get("routes").size() > 0) {
                    double distanceMeters = rootNode.get("routes").get(0).get("distance").asDouble();
                    
                    // Convert meters to kilometers (and round to 2 decimals)
                    double distanceKm = distanceMeters / 1000.0;
                    return Math.round(distanceKm * 100.0) / 100.0;
                }
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Distance Calculation Failed: " + e.getMessage());
            // Return -1 to indicate service unavailability without crashing the app
            return -1.0;
        }
        
        return -1.0;
    }
}
