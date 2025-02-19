package com.fxinject.core;

import com.fxinject.utils.FXInjectContainer;

/**
 * Facade class to simplify the usage of FXInject library.
 */
public class FXInject {
    /**
     * Creates a new dependency injection container.
     *
     * @return FXInjectContainer instance
     */
    public static FXInjectContainer createContainer() {
        return new FXInjectContainer();
    }

    /**
     * Creates an FXML loader with the given container.
     *
     * @param container The dependency injection container
     * @return FXMLDILoader instance
     */
    public static FXMLDILoader createFXMLLoader(FXInjectContainer container) {
        return new FXMLDILoader(container);
    }
}