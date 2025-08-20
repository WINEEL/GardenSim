package com.gardensim;

import javafx.scene.layout.GridPane;
import java.util.Arrays;

public class Tomato extends Plant {
    public Tomato() {
        super("Tomato", 65, 10, Arrays.asList("Aphids", "Caterpillars"));
    }
    public Tomato(GridPane gardenGrid) {
        super("Tomato", 65, 10, Arrays.asList("Aphids", "Caterpillars"), gardenGrid);
    }
}
