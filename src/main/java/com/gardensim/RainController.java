package com.gardensim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Controller responsible for handling rainfall simulation.
 * If rainfall is below a threshold, sprinklers are activated to compensate.
 */
public class RainController {
    private static final Logger log = LogManager.getLogger(RainController.class);

    /** Minimum rainfall units required before sprinklers are triggered. */
    private static final int RAINFALL_THRESHOLD = 5;

    private final SprinklerController sprinklerController = new SprinklerController();

    /**
     * Simulates rainfall in the garden. If rainfall is insufficient,
     * activates the sprinkler system to water the plants instead.
     *
     * @param rainfallAmount the amount of rainfall received
     * @param plants list of plants in the garden
     */
    public void simulateRain(int rainfallAmount, List<Plant> plants) {
        log.info("Simulating rain of {} units.", rainfallAmount);

        if (rainfallAmount < RAINFALL_THRESHOLD) {
            log.warn("Insufficient rainfall: {} units (threshold: {}). Activating sprinkler system.", rainfallAmount, RAINFALL_THRESHOLD);
            sprinklerController.activateSprinklers(plants);
        } else {
            for (Plant plant : plants) {
                plant.water(rainfallAmount);
            }
            log.info("Rainfall was sufficient. All plants watered with {} units.", rainfallAmount);
        }
    }
}
