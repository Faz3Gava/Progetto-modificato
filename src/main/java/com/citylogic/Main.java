package com.citylogic;

import com.citylogic.ui.CityLogicApp;

/**
 * Universal main entry point that does not directly extend javafx.application.Application.
 * This ensures clean execution from executable JARs without requiring JavaFX VM module hacks.
 */
public class Main {
    public static void main(String[] args) {
        CityLogicApp.main(args);
    }
}
