package com.fxinject.utils;

import com.fxinject.annotations.Component;
import com.fxinject.exceptions.FXInjectException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ClassPathScanner {
    private static final Logger logger = LoggerFactory.getLogger(ClassPathScanner.class);

    /**
     * Automatically determines the base package to scan based on the caller's package.
     *
     * @return Set of classes annotated with @Component
     */
    public static Set<Class<?>> findComponentClasses() {
        // Get the calling class (the class that called this method)
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callerClassName = stackTrace[2].getClassName();

        try {
            Class<?> callerClass = Class.forName(callerClassName);
            String basePackage = callerClass.getPackage().getName();

            // Scan from the base package of the caller
            return findComponentClasses(basePackage);
        } catch (ClassNotFoundException e) {
            logger.error("Could not determine caller's package", e);
            throw new FXInjectException("Failed to automatically scan packages", e);
        }
    }

    /**
     * Scans the specified package for classes annotated with @Component.
     *
     * @param basePackage The package to scan
     * @return Set of classes annotated with @Component
     */
    public static Set<Class<?>> findComponentClasses(String basePackage) {
        try {
            Reflections reflections = new Reflections(basePackage);
            Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);

            logger.info("Found {} components in package {}", componentClasses.size(), basePackage);
            return componentClasses;
        } catch (Exception e) {
            logger.error("Error scanning package: {}", basePackage, e);
            throw new FXInjectException("Failed to scan package: " + basePackage, e);
        }
    }
}