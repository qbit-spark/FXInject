package com.fxinject.core;

import com.fxinject.utils.FXInjectContainer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.net.URL;

/**
 * Custom FXML loader that integrates with FXInject dependency injection container.
 */
public class FXMLDILoader {
    private static final Logger logger = LoggerFactory.getLogger(FXMLDILoader.class);

    private final FXInjectContainer container;

    /**
     * Constructor for FXMLDILoader.
     *
     * @param container The FXInject dependency injection container
     */
    public FXMLDILoader(FXInjectContainer container) {
        this.container = container;
    }

    /**
     * Loads an FXML file using the dependency injection container.
     *
     * @param location URL of the FXML file
     * @return Loaded FXML Parent node
     * @throws IOException If there's an error loading the FXML file
     */
    public Parent load(URL location) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(location);

            // Use the container to create controllers
            loader.setControllerFactory(controllerClass ->
                    container.getComponent(controllerClass)
            );

            return loader.load();
        } catch (IOException e) {
            logger.error("Failed to load FXML: {}", location, e);
            throw e;
        }
    }
}