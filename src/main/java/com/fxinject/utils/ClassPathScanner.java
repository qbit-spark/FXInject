package com.fxinject.utils;

import com.fxinject.annotations.Component;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Utility class for scanning the classpath to find components.
 */
public class ClassPathScanner {
    private static final Logger logger = LoggerFactory.getLogger(ClassPathScanner.class);

    /**
     * Scans the specified package for classes annotated with @Component.
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
            return Set.of();
        }
    }
}