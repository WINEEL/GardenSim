package com.gardensim;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Entry point for the JavaFX application.
 * Loads the FXML UI and starts the Automated Garden simulation.
 */
public class GUIMain extends Application {
    private static final Logger log = LogManager.getLogger(GUIMain.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GUIMain.class.getResource("view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 840, 840);
        stage.setTitle("Automated Garden");
        stage.setScene(scene);
        stage.show();

        log.info("Automated Garden GUI launched successfully.");
    }
}
