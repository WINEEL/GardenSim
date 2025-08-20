package com.gardensim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class GardenSimulator {
    private static final Logger log = LogManager.getLogger(GardenSimulator.class);

    private static int dayCount = 0;

    public static void main(String[] args) {
        // ---- CLI / JVM properties (with safe defaults) ----
        final String configPath = System.getProperty("config", "/config.json"); // classpath resource
        final int days = parseInt(System.getProperty("days", "10"), 10);
        final double pestProb = clamp01(parseDouble(System.getProperty("pest", "0.30"), 0.30));
        final long tickMs = parseLong(System.getProperty("tickMs", "1000"), 1000L); // 1 day ~ 1s

        GardenSimulatorAPI api = new GardenSimulatorAPI();
        api.initializeGarden(configPath);

        Map<String, Object> initialPlantDetails = api.getPlants();
        log.info("Simulation config -> days={}, pestProb={}, tickMs={}ms, config={}",
                days, pestProb, tickMs, configPath);
        log.info("Initial plants: {}", initialPlantDetails);

        // Build a parasite pool from config; fallback to a small default set.
        List<String> parasitePool = api.getKnownParasiteNames();
        if (parasitePool.isEmpty()) {
            parasitePool = List.of("Aphids", "Caterpillar", "Whitefly", "Leafminer", "Hornworm");
            log.warn("Config had no parasite names; using fallback list: {}", parasitePool);
        }

        Random rng = new Random();

        for (int d = 1; d <= days; d++) {
            // Temperature (F) in a safe-ish range; your Plant logic handles extremes anyway.
            int tempF = 50 + rng.nextInt(46); // 50..95
            api.temperature(tempF);

            // Some rain on ~50% of the days
            if (rng.nextDouble() < 0.5) {
                int amount = 1 + rng.nextInt(5); // 1..5
                api.rain(amount);
            }

            // Pest event based on pestProb
            if (rng.nextDouble() < pestProb) {
                String pest = parasitePool.get(rng.nextInt(parasitePool.size()));
                api.parasites(pest);
            }

            // Advance simulated day
            sleepMs(tickMs);
        }

        // Summary
        api.getStatus();
        System.out.println("Ran " + days + " day(s) with pestProb=" + pestProb + " using config=" + configPath);
    }

    private static void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
            dayCount += 1;
            log.info("---------------- End of Day {} -----------------", dayCount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted: {}", e.getMessage());
        }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
    private static long parseLong(String s, long def) {
        try { return Long.parseLong(s); } catch (Exception e) { return def; }
    }
    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
