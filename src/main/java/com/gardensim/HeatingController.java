package com.gardensim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controller for managing the heating system in the garden.
 * Ensures plants are kept above a minimum safe temperature.
 */
public class HeatingController {
    private static final Logger log = LogManager.getLogger(HeatingController.class);

    /** Minimum safe temperature for plants in °F. */
    private static final int MINIMUM_SAFE_TEMPERATURE = 50;

    /**
     * Activates the heating system and raises the temperature
     * to the minimum safe threshold.
     *
     * @return the minimum safe temperature (°F).
     */
    public int activateHeating() {
        log.info("Activating heating system to increase temperature to {} °F.", MINIMUM_SAFE_TEMPERATURE);
        return MINIMUM_SAFE_TEMPERATURE;
    }
}
