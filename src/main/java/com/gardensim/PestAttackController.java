package com.gardensim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

/**
 * Controller responsible for simulating pest attacks on plants.
 */
public class PestAttackController {
    private static final Logger log = LogManager.getLogger(PestAttackController.class);

    /** Random generator for attack outcomes. */
    private final Random random = new Random();

    /** Tracks whether any plant was affected in the latest attack. */
    private boolean pestAttacked = false;

    /** Probability that a vulnerable plant will be killed by a pest attack. */
    private static final double ATTACK_PROBABILITY = 0.25;

    /**
     * Simulates a pest attack on the provided list of plants.
     *
     * @param selectedPest the pest type to simulate
     * @param plants       list of plants in the garden
     */
    public void simulatePestAttack(String selectedPest, List<Plant> plants) {
        log.info("Simulating pest attack: {}", selectedPest);
        pestAttacked = false;

        for (Plant plant : plants) {
            if (plant.getParasites().contains(selectedPest)) {
                pestAttacked = true;

                if (plant.isPesticideApplied()) {
                    log.info("Pesticide protects {} from the {} pest attack.", plant.getName(), selectedPest);
                } else {
                    if (random.nextDouble() < ATTACK_PROBABILITY) {
                        plant.setAlive(false);
                        log.warn("Plant {} has been killed by a {} pest attack.", plant.getName(), selectedPest);
                    } else {
                        log.warn("Plant {} resisted a {} pest attack and survived.", plant.getName(), selectedPest);
                    }
                }
            }
        }

        if (!pestAttacked) {
            log.info("Pest {} did not affect any plants.", selectedPest);
        }
    }
}
