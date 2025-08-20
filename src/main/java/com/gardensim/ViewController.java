package com.gardensim;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Final ViewController:
 * - Single background image on the GridPane (no per-cell soil tiles).
 * - Fixed 7x8 grid using percent constraints (cells won’t resize when clicking).
 * - Click a cell to plant: Rose, Tomato or Orange (radio buttons on the right).
 * - Sprinklers / Rain / Pesticide / Temperature actions wired.
 * - Pests overlay + removal, with proper accessors on Plant (getNumPests/setNumPests).
 * - All resources loaded from /images/... via classpath (safeImage).
 *
 * Needed images in src/main/resources/images:
 *   garden.jpg  (background)
 *   rose.png, tomato.png, orange.png
 *   sunny.png, rain.png
 *   bug.png     (pest)
 */

public class ViewController {
    private Timeline simTimeline;
    private static final int STEP_SECONDS = 10;      // 1 day every 3 seconds (adjust)
    private static final double PEST_SPAWN_PROB = 0.30;  // 40% chance per occupied cell per day

    private static final Logger log = LogManager.getLogger(ViewController.class);

    // ---- Images (loaded safely; null allowed so app still runs) ----
    private static final Image ROSE_IMG   = safeImage("/images/rose.png");
    private static final Image TOMATO_IMG = safeImage("/images/tomato.png");
    private static final Image ORANGE_IMG = safeImage("/images/orange.png");
    private static final Image PEST_IMG   = safeImage("/images/bug.png");
    private static final Image SUNNY_IMG  = safeImage("/images/sunny.png");
    private static final Image RAINY_IMG  = safeImage("/images/rain.png");

    // ---- Controllers ----
    private final HeatingController     heatingController     = new HeatingController();
    private final SprinklerController   sprinklerController   = new SprinklerController();
    private final PesticideController   pesticideController   = new PesticideController();
    private final RainController        rainController        = new RainController();
    private final TemperatureController temperatureController = new TemperatureController();

    // ---- FXML-injected UI ----
    @FXML private GridPane gardenGrid;

    // Right sidebar
    @FXML private ImageView weatherImageView;
    @FXML private Label weatherLabel;

    @FXML private RadioButton roseButton;
    @FXML private RadioButton TomatoButton;   // keep FXML ids
    @FXML private RadioButton OrangeButton;   // keep FXML ids

    @FXML private Button heatingButton;
    @FXML private Button sprinklersButton;
    @FXML private Button pesticideButton;
    @FXML private Button rainButton;

    // Top/bottom
    @FXML private Button iterateDayButton;
    @FXML private Label userInfoLabel;

    // Logs
    @FXML private TextArea logArea;

    // ---- State ----
    private int day = 1;

    /** map for pests -> their image views */
    public static final Map<Pest, ImageView> pestImageViewMap = new HashMap<>();

    /** occupied plant cells keyed by "row,col" */
    public static final Set<String> occupiedCells = new HashSet<>();

    // ----------------------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------------------
    @FXML
    public void initialize() {
        // background
        var bgUrl = getClass().getResource("/images/garden.jpg");
        if (bgUrl != null) {
            gardenGrid.setStyle(
                    "-fx-background-image: url('" + bgUrl.toExternalForm() + "');" +
                            "-fx-background-repeat: stretch;" +
                            "-fx-background-size: cover;" +
                            "-fx-background-position: center center;"
            );
        } else {
            logBoth("Missing /images/garden.jpg (background).");
        }

        // fixed, uniform tracks then pre-populate cell containers
        int rows = 7, cols = 8;
        lockGridToUniformCells(rows, cols);
        populateEmptyCells(rows, cols);

        userInfoLabel.setText("   Today is Day-" + day);
        setWeatherSunny();
        if (roseButton != null) roseButton.setSelected(true);

        if (ROSE_IMG == null) log.warn("Missing /images/rose.png");
        else log.info("rose.png loaded OK");

        logBoth("Garden GUI initialized (background-only grid).");
    }

    // ----------------------------------------------------------------
    // Planting by clicking the GridPane (center area)
    // ----------------------------------------------------------------
    // FXML: onMouseClicked="#plantPlants"
    @FXML
    private void plantPlants(MouseEvent event) {
        int rows = gardenGrid.getRowConstraints().isEmpty() ? 7 : gardenGrid.getRowConstraints().size();
        int cols = gardenGrid.getColumnConstraints().isEmpty() ? 8 : gardenGrid.getColumnConstraints().size();

        double cellW = gardenGrid.getWidth()  / Math.max(cols, 1);
        double cellH = gardenGrid.getHeight() / Math.max(rows, 1);

        int col = (int) Math.floor(event.getX() / Math.max(cellW, 1));
        int row = (int) Math.floor(event.getY() / Math.max(cellH, 1));

        // clamp
        col = Math.max(0, Math.min(cols - 1, col));
        row = Math.max(0, Math.min(rows - 1, row));

        try {
            if (roseButton != null && roseButton.isSelected()) {
                plantRose(row, col);
            } else if (TomatoButton != null && TomatoButton.isSelected()) {
                plantTomato(row, col);
            } else if (OrangeButton != null && OrangeButton.isSelected()) {
                plantOrange(row, col);
            } else {
                logBoth("No plant type selected.");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Create or get the cell container at (row,col). We use a StackPane that fills the grid track.
    private StackPane getCellBox(int row, int col) {
        for (Node n : gardenGrid.getChildren()) {
            // Skip the grid-lines Group or any non-cell nodes
            if (!(n instanceof StackPane)) continue;

            Integer r = GridPane.getRowIndex(n);
            Integer c = GridPane.getColumnIndex(n);
            int rr = (r == null) ? 0 : r;
            int cc = (c == null) ? 0 : c;

            if (rr == row && cc == col) return (StackPane) n;
        }

        // Shouldn't happen (we pre-create cells), but keep a safe fallback.
        StackPane cell = new StackPane();
        cell.setMinSize(0, 0);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setVgrow(cell, Priority.ALWAYS);
        gardenGrid.add(cell, col, row); // (column, row)
        return cell;
    }

    // Add an image centered in a cell and bind its size (never affects layout)
    private ImageView addPlantImageToCell(StackPane container, Image img) {
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);
        iv.setMouseTransparent(true);
        var side = Bindings.min(container.widthProperty(), container.heightProperty());
        iv.fitWidthProperty().bind(side.multiply(0.8));
        iv.fitHeightProperty().bind(side.multiply(0.8));
        StackPane.setAlignment(iv, javafx.geometry.Pos.CENTER);
        container.getChildren().add(iv);
        iv.toFront();
        return iv;
    }

    // ----------------------------------------------------------------
    // Plant methods
    // ----------------------------------------------------------------
    public void plantRose(int row, int col) throws FileNotFoundException {
        String key = row + "," + col;
        if (occupiedCells.contains(key)) return;

        StackPane container = getCellBox(row, col);
        ImageView iv = addPlantImageToCell(container, ROSE_IMG);

        occupiedCells.add(key);

        Rose rose = new Rose(gardenGrid);
        rose.setRow(row);
        rose.setCol(col);
        Plant.plantImageViewMap.put(rose, iv);
        Plant.plantsList.add(rose);

        logBoth("Planted Rose at (" + row + "," + col + ").");
    }

    public void plantTomato(int row, int col) throws FileNotFoundException {
        String key = row + "," + col;
        if (occupiedCells.contains(key)) return;

        StackPane container = getCellBox(row, col);
        ImageView iv = addPlantImageToCell(container, TOMATO_IMG);

        occupiedCells.add(key);

        Tomato tomato = new Tomato(gardenGrid);
        tomato.setRow(row);
        tomato.setCol(col);
        Plant.plantImageViewMap.put(tomato, iv);
        Plant.plantsList.add(tomato);

        logBoth("Planted Tomato at (" + row + "," + col + ").");
    }

    public void plantOrange(int row, int col) throws FileNotFoundException {
        String key = row + "," + col;
        if (occupiedCells.contains(key)) return;

        StackPane container = getCellBox(row, col);
        ImageView iv = addPlantImageToCell(container, ORANGE_IMG);

        occupiedCells.add(key);

        Orange orange = new Orange(gardenGrid);
        orange.setRow(row);
        orange.setCol(col);
        Plant.plantImageViewMap.put(orange, iv);
        Plant.plantsList.add(orange);

        logBoth("Planted Orange at (" + row + "," + col + ").");
    }

    // ----------------------------------------------------------------
    // Buttons: Heating / Sprinklers / Pesticide / Rain
    // ----------------------------------------------------------------
    @FXML
    private void adjustTemperature(ActionEvent event) {
        Platform.runLater(() -> {
            int currentTemperature = getCurrentTemperature();
            temperatureController.adjustTemperature(currentTemperature, Plant.plantsList);
            handleTemperatureWarnings(currentTemperature);
        });
    }

    private void handleTemperatureWarnings(int temperature) {
        if (temperature < TemperatureController.LOWER_TEMPERATURE_THRESHOLD) {
            int adjusted = heatingController.activateHeating();
            logBoth("Warning: Low temperature detected. Heating activated. Current temperature: " + adjusted + "°F");
            setWeatherSunny();
        } else if (temperature > 120) {
            logBoth("Extreme high temperature detected. Plants may die.");
        } else {
            logBoth("Temperature is within the normal range.");
        }
    }

    private int getCurrentTemperature() {
        // Hook up to a control if needed
        return 24;
    }

    @FXML
    private void activateSprinklers(ActionEvent event) {
        sprinklerController.activateSprinklers(Plant.plantsList);
        logBoth("Sprinklers activated.");
    }

    @FXML
    private void applyPesticide(ActionEvent event) {
        pesticideController.applyPesticide(Plant.plantsList);
        removePestsImmediately();
        logBoth("Pesticide applied across the garden.");
    }

    private void removePestsImmediately() {
        Platform.runLater(() -> {
            int removed = 0;
            List<Pest> toRemove = new ArrayList<>();
            for (Pest pest : Pest.pests) {
                ImageView pestView = pestImageViewMap.get(pest);
                if (pestView != null) {
                    Pane cell = (Pane) pestView.getParent();
                    cell.getChildren().remove(pestView);
                    toRemove.add(pest);
                    pestImageViewMap.remove(pest);
                    removed++;
                }
            }
            Pest.pests.removeAll(toRemove);
            logBoth("Pesticide removed " + removed + " pests.");
        });
    }

    @FXML
    private void activateRain(ActionEvent event) {
        int rainfall = 10; // example
        rainController.simulateRain(rainfall, Plant.plantsList);
        adjustSprinklersBasedOnRainfall(rainfall);

        Platform.runLater(() -> {
            setWeatherRainy();
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(10), e -> setWeatherSunny()));
            t.play();
        });
    }

    private void adjustSprinklersBasedOnRainfall(int rainfallAmount) {
        Platform.runLater(() -> {
            if (rainfallAmount <= 5) {
                logBoth("Insufficient rainfall. Activating sprinklers.");
                sprinklerController.activateSprinklers(Plant.plantsList);
            } else {
                logBoth("Sufficient rainfall. No sprinklers needed.");
            }
        });
    }

    // ----------------------------------------------------------------
    // Day iteration & pests
    // ----------------------------------------------------------------
    @FXML
    private void iterateDayWithTimer(ActionEvent e) {
        if (simTimeline != null && simTimeline.getStatus() == Animation.Status.RUNNING) {
            simTimeline.stop();
            if (iterateDayButton != null) iterateDayButton.setText("Simulate");
            logBoth("Simulation paused.");
            return;
        }

        simTimeline = new Timeline(new KeyFrame(Duration.seconds(STEP_SECONDS), ev -> runOneDay()));
        simTimeline.setCycleCount(Timeline.INDEFINITE);
        simTimeline.play();
        if (iterateDayButton != null) iterateDayButton.setText("Pause");
        logBoth("Simulation started (1 day every " + STEP_SECONDS + "s).");
    }

    private void runOneDay() {
        day++;
        if (userInfoLabel != null) userInfoLabel.setText("Today is Day " + day);

        // 1) Existing pests can hurt plants this morning.
        pestKillPlant();

        // 2) Optionally thin pests every other day so it doesn't explode.
        if (day % 2 == 0) {
            pestControl();
        }

        // 3) Spawn NEW pests for tomorrow; they stay visible all day.
        addPestsToCells();

        logBoth("End of day " + day + ".");
    }

    private void addPestsToCells() {
        Random random = new Random();
        int spawned = 0;

        for (String key : occupiedCells) {
            if (random.nextDouble() < PEST_SPAWN_PROB) {
                String[] parts = key.split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);

                Pane cell = getCellBox(row, col);   // StackPane
                ImageView pestView = new ImageView(PEST_IMG);
                pestView.setFitWidth(28);
                pestView.setFitHeight(28);
                pestView.setMouseTransparent(true);
                cell.getChildren().add(pestView);
                pestView.toFront();

                Pest pest = new Pest(row, col, 0);
                pestImageViewMap.put(pest, pestView);
                Pest.pests.add(pest);

                for (Plant plant : Plant.plantsList) {
                    if (plant.getRow() == row && plant.getCol() == col) {
                        plant.setNumPests(plant.getNumPests() + 1);
                    }
                }
                spawned++;
                logBoth("Pest spawned at (" + row + "," + col + ").");
            }
        }

        if (spawned == 0) logBoth("No new pests today.");
    }

    private void pestControl() {
        List<Pest> toRemove = new ArrayList<>();
        Random ran = new Random();
        int removed = 0;

        for (Pest pest : Pest.pests) {
            if (ran.nextInt(8) != 1) { // remove most pests
                ImageView pestView = pestImageViewMap.get(pest);
                if (pestView != null) {
                    Pane cell = (Pane) pestView.getParent();
                    cell.getChildren().remove(pestView);
                    pestImageViewMap.remove(pest);
                    toRemove.add(pest);
                    removed++;
                }
            }
        }
        Pest.pests.removeAll(toRemove);
        if (removed > 0) logBoth("Pest control removed " + removed + " pests.");
    }

    private void pestKillPlant() {
        Runnable task = () -> {
            List<Plant> plantsToRemove = new ArrayList<>();
            List<Pest> pestsToRemove = new ArrayList<>();
            int plantsKilled = 0;

            for (Plant plant : Plant.plantsList) {
                int row = plant.getRow();
                int col = plant.getCol();

                // Kill only if a pest currently exists in this cell
                boolean pestHere = Pest.pests.stream().anyMatch(p -> p.getRow() == row && p.getCol() == col);
                if (!pestHere) continue;

                String key = row + "," + col;
                ImageView plantView = Plant.plantImageViewMap.get(plant);
                if (plantView != null) {
                    Pane cell = (Pane) plantView.getParent();
                    cell.getChildren().remove(plantView);
                    Plant.plantImageViewMap.remove(plant);
                    occupiedCells.remove(key);
                    plantsToRemove.add(plant);
                    plantsKilled++;

                    // Remove pests in that same cell (visual + model)
                    for (Pest pest : new ArrayList<>(Pest.pests)) {
                        if (pest.getRow() == row && pest.getCol() == col) {
                            ImageView pestView = pestImageViewMap.get(pest);
                            if (pestView != null) cell.getChildren().remove(pestView);
                            pestImageViewMap.remove(pest);
                            pestsToRemove.add(pest);
                        }
                    }
                }
            }

            Plant.plantsList.removeAll(plantsToRemove);
            Pest.pests.removeAll(pestsToRemove);
            if (plantsKilled > 0) logBoth(plantsKilled + " plant(s) died from pests.");
        };

        if (Platform.isFxApplicationThread()) {
            task.run();          // run now (before we spawn today's pests)
        } else {
            Platform.runLater(task);
        }
    }

    // ----------------------------------------------------------------
    // Finish
    // ----------------------------------------------------------------
    @FXML
    private void finish(ActionEvent e) {
        System.exit(0);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private static Image safeImage(String resourcePath) {
        try {
            var in = ViewController.class.getResourceAsStream(resourcePath);
            return (in == null) ? null : new Image(in);
        } catch (Exception e) {
            LogManager.getLogger(ViewController.class).warn("Failed to load: {}", resourcePath, e);
            return null;
        }
    }

    private void setWeatherSunny() {
        if (weatherLabel != null) weatherLabel.setText("Sunny");
        if (weatherImageView != null && SUNNY_IMG != null) weatherImageView.setImage(SUNNY_IMG);
    }

    private void setWeatherRainy() {
        if (weatherLabel != null) weatherLabel.setText("Rainy");
        if (weatherImageView != null && RAINY_IMG != null) weatherImageView.setImage(RAINY_IMG);
    }

    private void logBoth(String msg) {
        log.info(msg);
        if (logArea != null) Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }

    /**
     * Force equal-sized rows/cols via percent constraints so content never
     * warps the grid. Use this instead of per-cell filler nodes.
     */
    private void lockGridToUniformCells(int rows, int cols) {
        gardenGrid.getColumnConstraints().clear();
        for (int c = 0; c < cols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / cols);
            cc.setFillWidth(true);
            cc.setHgrow(Priority.ALWAYS);
            gardenGrid.getColumnConstraints().add(cc);
        }
        gardenGrid.getRowConstraints().clear();
        for (int r = 0; r < rows; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / rows);
            rc.setFillHeight(true);
            rc.setVgrow(Priority.ALWAYS);
            gardenGrid.getRowConstraints().add(rc);
        }
        gardenGrid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        gardenGrid.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        gardenGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    /** Create transparent StackPane containers in every grid cell (7x8). */
    private void populateEmptyCells(int rows, int cols) {
        gardenGrid.getChildren().removeIf(n -> n instanceof StackPane); // clean any old cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                StackPane cell = new StackPane();
                cell.setMinSize(0, 0);
                cell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                GridPane.setFillWidth(cell, true);
                GridPane.setFillHeight(cell, true);
                GridPane.setHgrow(cell, Priority.ALWAYS);
                GridPane.setVgrow(cell, Priority.ALWAYS);
                gardenGrid.add(cell, c, r); // NOTE: (col, row)
            }
        }
    }
}
