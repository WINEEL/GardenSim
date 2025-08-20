package com.gardensim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Controller responsible for activating sprinklers when rainfall is insufficient.
 * Provides water to all plants based on the average water requirement.
 */
public class SprinklerController {
    private static final Logger log = LogManager.getLogger(SprinklerController.class);

    /**
     * Activates the sprinkler system and waters all plants.
     *
     * @param plants list of plants in the garden
     */
    public void activateSprinklers(List<Plant> plants) {
        if (plants == null || plants.isEmpty()) {
            log.warn("No plants available to water. Sprinklers not activated.");
            return;
        }

        int averageWaterRequirement = calculateAverageWaterRequirement(plants);
        log.info("Activating sprinklers, providing an average of {} units of water to all plants.", averageWaterRequirement);

        for (Plant plant : plants) {
            plant.water(averageWaterRequirement);
        }
    }

    /** Calculates the average water requirement across all plants. */
    private int calculateAverageWaterRequirement(List<Plant> plants) {
        int totalRequirement = 0;
        for (Plant plant : plants) {
            totalRequirement += plant.getWaterRequirement();
        }
        return plants.isEmpty() ? 0 : totalRequirement / plants.size();
    }
}
