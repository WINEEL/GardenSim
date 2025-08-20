package com.gardensim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Headless API used by graders/CI. Loads plants from a JSON config (classpath)
 * and exposes simple actions (rain, temperature, parasites). Designed to run
 * without JavaFX.
 */
public class GardenSimulatorAPI {
    private static final Logger log = LogManager.getLogger(GardenSimulatorAPI.class);

    private final List<Plant> plants = new ArrayList<>();
    private GardenController gardenController;

    /** Initializes the garden using a classpath resource (e.g. "/config.json"). */
    public void initializeGarden(String resourcePath) {
        plants.clear();
        loadPlants(resourcePath);
        log.info("Garden initialized with plants: {}", plants);
        gardenController = new GardenController(plants);
    }

    /** Convenience: initialize with default classpath "/config.json". */
    public void initializeGarden() {
        initializeGarden("/config.json");
    }

    /** Loads plant definitions from a config JSON file on the classpath. */
    public void loadPlants(String resourcePath) {
        String normalized = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        try (InputStream is = GardenSimulatorAPI.class.getResourceAsStream(normalized)) {
            if (is == null) {
                log.warn("Resource '{}' not found. Falling back to built-in defaults.", normalized);
                addDefaultPlants();
                return;
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(content);
            JSONArray plantsArray = json.getJSONArray("plants");

            for (int i = 0; i < plantsArray.length(); i++) {
                JSONObject p = plantsArray.getJSONObject(i);
                String name = p.getString("name");
                int waterRequirement = p.getInt("waterRequirement");
                int temperature = p.getInt("temperature");

                List<String> parasites = new ArrayList<>();
                JSONArray par = p.getJSONArray("parasites");
                for (int j = 0; j < par.length(); j++) {
                    parasites.add(par.getString(j));
                }

                // IMPORTANT: matches Plant(String name, int temperature, int waterRequirement, List<String> parasites)
                plants.add(new Plant(name, temperature, waterRequirement, parasites));
            }
        } catch (Exception e) {
            log.error("Failed to load '{}': {}. Using defaults.", normalized, e.toString());
            addDefaultPlants();
        }
    }

    private void addDefaultPlants() {
        plants.add(new Plant("Rose",   22, 20, List.of("Aphids", "Caterpillar")));
        plants.add(new Plant("Tomato", 24, 18, List.of("Hornworm", "Whitefly")));
        plants.add(new Plant("Orange", 26, 22, List.of("Aphids", "Leafminer")));
    }

    /** Returns plant information as a map of names, water requirements, and parasites. */
    public Map<String, Object> getPlants() {
        List<String> names = new ArrayList<>();
        List<Integer> waterReqs = new ArrayList<>();
        List<List<String>> parasiteLists = new ArrayList<>();

        for (Plant plant : plants) {
            if (plant.isAlive()) {
                names.add(plant.getName());
                waterReqs.add(plant.getWaterRequirement());
                parasiteLists.add(plant.getParasites());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("plants", names);
        result.put("waterRequirement", waterReqs);
        result.put("parasites", parasiteLists);
        return result;
    }

    /** Flattened parasite names from all configured plants. */
    public List<String> getKnownParasiteNames() {
        Set<String> set = new LinkedHashSet<>();
        for (Plant p : plants) {
            List<String> ps = p.getParasites();
            if (ps != null) set.addAll(ps);
        }
        return new ArrayList<>(set);
    }

    // ----- Actions (headless) -----

    /** Simulates rainfall in the garden asynchronously. */
    public void rain(int amount) {
        new GardenThread(() -> {
            gardenController.simulateRain(amount);
            log.info("It rained {} unit(s).", amount);
        }, "rainThread").start();
    }

    /** Simulates a temperature change in the garden asynchronously. */
    public void temperature(int temperatureF) {
        new GardenThread(() -> {
            log.info("Temperature reached {} F", temperatureF);
            gardenController.simulateTemperature(temperatureF);
        }, "temperatureThread").start();
    }

    /** Simulates a parasite attack on the garden. */
    public void parasites(String parasiteName) {
        log.info("Parasite {} infested the garden", parasiteName);
        gardenController.simulatePestAttack(parasiteName);
    }

    /** Logs the current status of alive and dead plants. */
    public void getStatus() {
        log.info("Alive Plants : {}", gardenController.getAlivePlants());
        log.info("Dead Plants  : {}", gardenController.getDeadPlants());
    }
}
