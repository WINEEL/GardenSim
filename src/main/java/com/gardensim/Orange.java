package com.gardensim;

import javafx.scene.layout.GridPane;
import java.util.Arrays;

public class Orange extends Plant {
    public Orange() {
        super("Orange", 70, 15, Arrays.asList("Aphids", "Leafhoppers"));
    }
    public Orange(GridPane gardenGrid) {
        super("Orange", 70, 15, Arrays.asList("Aphids", "Leafhoppers"), gardenGrid);
    }
}
