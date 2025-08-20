package com.gardensim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Controller responsible for adjusting garden temperature
 * and ensuring plants remain within safe temperature thresholds.
 */
public class TemperatureController {
    /** Lower temperature threshold below which heating is activated. */
    public static final int LOWER_TEMPERATURE_THRESHOLD = 40;

    /** Upper temperature threshold beyond which plants cannot survive. */
    public static final int UPPER_TEMPERATURE_THRESHOLD = 120;

    private static final Logger log = LogManager.getLogger(TemperatureController.class);
    private final HeatingController heatingController = new HeatingController();

    /**
     * Adjusts the garden's temperature and applies it to all plants.
     *
     * @param temperature the current temperature in °F
     * @param plants list of plants in the garden
     */
    public void adjustTemperature(int temperature, List<Plant> plants) {
        log.info("Adjusting temperature to {} °F.", temperature);

        if (temperature < LOWER_TEMPERATURE_THRESHOLD) {
            log.warn("Detected low temperature of {} °F (below {}). Activating heating system.", temperature, LOWER_TEMPERATURE_THRESHOLD);
            temperature = heatingController.activateHeating();
        } else if (temperature > UPPER_TEMPERATURE_THRESHOLD) {
            log.warn("Extreme high temperature detected ({} °F). Plants may not survive.", temperature);
        }

        adjustPlantTemperatures(plants, temperature);
    }

    /** Applies the given temperature to all plants in the garden. */
    private void adjustPlantTemperatures(List<Plant> plants, int temperature) {
        for (Plant plant : plants) {
            plant.adjustTemperature(temperature);
        }
        log.info("Temperature {} °F applied to {} plants.", temperature, plants.size());
    }
}
