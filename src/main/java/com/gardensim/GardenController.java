package com.gardensim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GardenController {
    @SuppressWarnings("unused")
    private static final Logger log = LogManager.getLogger(GardenController.class);

    private List<Plant> plants;
    private RainController rainController;
    private TemperatureController temperatureController;
    private PestAttackController pestAttackController;
    private PesticideController pesticideController;
    private Random random = new Random();

    public GardenController(List<Plant> plants) {
        this.plants = plants;
        this.rainController = new RainController();
        this.temperatureController = new TemperatureController();
        this.pestAttackController = new PestAttackController();
        this.pesticideController = new PesticideController();
    }

    /** Simulates rainfall effects on plants. */
    void simulateRain(int rainfall) {
        rainController.simulateRain(rainfall, plants);
    }

    /** Simulates temperature adjustment effects on plants. */
    void simulateTemperature(int temperature) {
        temperatureController.adjustTemperature(temperature, plants);
    }

    /**
     * Simulates a pest attack on the garden.
     * Randomly decides whether to apply pesticide before the attack.
     */
    void simulatePestAttack(String pest) {
        if (random.nextBoolean()) {
            pesticideController.applyPesticide(plants);
        }
        pestAttackController.simulatePestAttack(pest, plants);
    }

    /** Returns a list of alive plants by name. */
    public List<String> getAlivePlants() {
        List<String> alive = new ArrayList<>();
        plants.forEach(plant -> {
            if (plant.isAlive()) {
                alive.add(plant.getName());
            }
        });
        return alive;
    }

    /** Returns a list of dead plants by name. */
    public List<String> getDeadPlants() {
        List<String> dead = new ArrayList<>();
        plants.forEach(plant -> {
            if (!plant.isAlive()) {
                dead.add(plant.getName());
            }
        });
        return dead;
    }
}
