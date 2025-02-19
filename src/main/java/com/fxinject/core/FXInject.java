package com.fxinject.core;

import com.fxinject.annotations.EnableFxInject;
import com.fxinject.utils.FXInjectContainer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

public class FXInject {
    private static FXInjectContainer container;

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

    public static FXMLLoader createFXMLLoader(URL location) {
        if (container == null) {
            throw new IllegalStateException("FXInject has not been initialized. Make sure your Application class is annotated with @EnableFxInject.");
        }
        FXMLLoader loader = new FXMLLoader(location);
        loader.setControllerFactory(container::getComponent);
        return loader;
    }

    public static Parent loadFXML(URL location) throws IOException {
        return createFXMLLoader(location).load();
    }
}