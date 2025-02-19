package com.fxinject.core;

import com.fxinject.annotations.EnableFxInject;
import com.fxinject.utils.FXInjectContainer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * FXInject is the main entry point for the FXInject library.
 * It provides static methods to initialize the dependency injection container
 * and create FXML loaders that work with the container.
 */
public class FXInject {
    private static final Logger logger = LoggerFactory.getLogger(FXInject.class);
    private static FXInjectContainer container;

    /**
     * Initializes the FXInject container for a JavaFX application.
     * This should be called in the start method of the Application class.
     * @param application The JavaFX application instance
     */
    public static void initializeForApplication(Application application) {
        Class<?> appClass = application.getClass();
        EnableFxInject annotation = appClass.getAnnotation(EnableFxInject.class);

        if (annotation != null) {
            container = new FXInjectContainer();
            String[] basePackages = annotation.basePackages();
            if (basePackages.length > 0) {
                for (String basePackage : basePackages) {
                    container.scan(basePackage);
                }
            } else {
                container.scan(appClass.getPackage().getName());
            }
        }
    }

    /**
     * Creates an FXMLLoader that works with the FXInject container.
     * @param location The URL of the FXML file to load
     * @return An FXMLLoader instance
     */
    public static FXMLLoader createFXMLLoader(URL location) {
        if (container == null) {
            logger.warn("FXInject container has not been initialized. Creating a new container without scanning for components.");
            container = new FXInjectContainer();
        }
        FXMLLoader loader = new FXMLLoader(location);
        loader.setControllerFactory(type -> {
            Object component = container.getComponent(type);
            if (component == null) {
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    logger.error("Failed to create instance of {}", type.getName(), e);
                    return null;
                }
            }
            return component;
        });
        return loader;
    }

    /**
     * Loads an FXML file using the FXInject container.
     * @param location The URL of the FXML file to load
     * @return The root Parent node of the loaded FXML
     * @throws IOException If there's an error loading the FXML file
     */
    public static Parent loadFXML(URL location) throws IOException {
        return createFXMLLoader(location).load();
    }
}