/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cycle_nest.api;

import org.glassfish.jersey.server.ResourceConfig;
/**
 *
 * @author ShadowSCI
 */
public class AppConfig extends ResourceConfig {
    public AppConfig() {
        // Hard-code the package scan here
        packages("com.cycle_nest.api");
        
        // Debug line: This will print in the Output window if Jersey starts
        System.out.println(">>> CYCLENEST API IS STARTING... <<<");
    }
}
