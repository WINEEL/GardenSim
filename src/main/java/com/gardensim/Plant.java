package com.gardensim;

import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a plant in the garden simulation.
 * Tracks its state including water, temperature, pests, and pesticide application.
 */
public class Plant {
    /** Maps plants to their associated image views for GUI rendering. */
    public static final HashMap<Plant, ImageView> plantImageViewMap = new HashMap<>();

    /** Global list of all plants in the simulation. */
    public static final List<Plant> plantsList = new ArrayList<>();

    /** Number of pests currently affecting this plant (if tracked). */
    private int numPests;

    private int row;
    private int col;
    private String name;
    private int waterRequirement;
    private List<String> parasites;
    private int currentWaterLevel;
    private int temperature;
    private boolean isAlive;
    private boolean pesticideApplied;
    private GridPane gardenGrid;

    public Plant(String name, int temperature, int waterRequirement, List<String> parasites) {
        this.name = name;
        this.waterRequirement = waterRequirement;
        this.parasites = parasites;
        this.currentWaterLevel = 0;
        this.temperature = temperature;
        this.isAlive = true;
    }

    public Plant(String name, int temperature, int waterRequirement, List<String> parasites, GridPane gardenGrid) {
        this(name, temperature, waterRequirement, parasites);
        this.gardenGrid = gardenGrid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name; }

    public int getWaterRequirement() {
        return waterRequirement;
    }
    public void setWaterRequirement(int waterRequirement) { this.waterRequirement = waterRequirement; }

    public List<String> getParasites() {
        return parasites;
    }
    public void setParasites(List<String> parasites) { this.parasites = parasites; }

    public int getCurrentWaterLevel() {
        return currentWaterLevel;
    }
    public void setCurrentWaterLevel(int currentWaterLevel) { this.currentWaterLevel = currentWaterLevel; }

    public int getTemperature() {
        return temperature;
    }
    public void setTemperature(int temperature) { this.temperature = temperature; }

    public boolean isAlive() {
        return isAlive;
    }
    public void setAlive(boolean isAlive) { this.isAlive = isAlive; }

    public boolean isPesticideApplied() {
        return pesticideApplied;
    }
    public void setPesticideApplied(boolean pesticideApplied) { this.pesticideApplied = pesticideApplied; }

    public int getRow() {
        return row;
    }
    public void setRow(int row) { this.row = row; }

    public int getCol() {
        return col;
    }
    public void setCol(int col) { this.col = col; }

    public int getNumPests() {
        return numPests;
    }
    public void setNumPests(int numPests) { this.numPests = numPests; }

    public GridPane getGardenGrid() {
        return gardenGrid;
    }
    public void setGardenGrid(GridPane gardenGrid) { this.gardenGrid = gardenGrid; }

    /** Waters the plant. Over-watering (> 2× requirement) kills it. */
    public void water(int amount) {
        currentWaterLevel += amount;
        if (currentWaterLevel > waterRequirement * 2) {
            isAlive = false;
        }
    }

    /** Adjusts temperature; extreme temperatures (<40 or >120 °F) kill the plant. */
    public void adjustTemperature(int temp) {
        temperature = temp;
        if (temperature < 40 || temperature > 120) {
            isAlive = false;
        }
    }

    /** Infests the plant with a pest; kills it if the pest is in its vulnerability list. */
    public void infest(String type) {
        if (parasites.contains(type)) {
            isAlive = false;
        }
    }

    @Override
    public String toString() {
        return "Plant{" +
                "name='" + name + '\'' +
                ", currentWaterLevel=" + currentWaterLevel +
                ", waterRequirement=" + waterRequirement +
                ", temperature=" + temperature +
                ", isAlive=" + isAlive +
                '}';
    }
}
