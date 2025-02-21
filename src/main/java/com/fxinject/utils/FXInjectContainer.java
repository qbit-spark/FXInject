package com.fxinject.utils;

import com.fxinject.annotations.Autowired;
import com.fxinject.annotations.Inject;
import com.fxinject.exceptions.FXInjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FXInjectContainer is the core dependency injection container for the FXInject library.
 * It handles component discovery, instantiation, and dependency injection.
 */
public class FXInjectContainer {
    private static final Logger logger = LoggerFactory.getLogger(FXInjectContainer.class);

    // Stores instantiated components, using ConcurrentHashMap for thread safety
    private final Map<Class<?>, Object> components = new ConcurrentHashMap<>();

    /**
     * Scans for components starting from the caller's package.
     */
    public void scan() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callerClassName = stackTrace[2].getClassName();

        try {
            Class<?> callerClass = Class.forName(callerClassName);
            String basePackage = callerClass.getPackage().getName();
            scan(basePackage);
        } catch (ClassNotFoundException e) {
            logger.error("Could not determine caller's package", e);
        }
    }

    /**
     * Scans the specified base package for components and sets up dependency injection.
     * @param basePackage The root package to scan for components
     */
    public void scan(String basePackage) {
        Set<Class<?>> componentClasses = ClassPathScanner.findComponentClasses(basePackage);

        if (componentClasses.isEmpty()) {
            logger.warn("No components found in package: {}. The container will still function, but no dependencies will be available for injection.", basePackage);
            return;
        }

        // First pass: create instances of all discovered components
        componentClasses.forEach(this::instantiateComponent);
        // Second pass: inject dependencies into created components
        components.values().forEach(this::injectDependencies);
        logComponentSummary();
    }

    /**
     * Creates an instance of a component class.
     * @param componentClass The class to instantiate
     */
    private void instantiateComponent(Class<?> componentClass) {
        try {
            Constructor<?> constructor = findInjectableConstructor(componentClass);
            constructor.setAccessible(true);
            Object[] params = resolveConstructorParameters(constructor);
            Object instance = constructor.newInstance(params);
            components.put(componentClass, instance);
        } catch (Exception e) {
            logger.error("Failed to instantiate component: {}", componentClass.getName(), e);
        }
    }

    /**
     * Finds the most appropriate constructor for component instantiation.
     * @param clazz The class to find a constructor for
     * @return The selected constructor
     */
//    private Constructor<?> findInjectableConstructor(Class<?> clazz) {
//        Constructor<?>[] constructors = clazz.getConstructors();
//        Optional<Constructor<?>> annotatedConstructor = Arrays.stream(constructors)
//                .filter(constructor ->
//                        constructor.isAnnotationPresent(Inject.class) ||
//                                constructor.isAnnotationPresent(Autowired.class))
//                .findFirst();
//
//        return annotatedConstructor.orElseGet(() -> {
//            try {
//                return clazz.getConstructor();
//            } catch (NoSuchMethodException e) {
//                return clazz.getConstructors()[0];
//            }
//        });
//    }


    private Constructor<?> findInjectableConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Optional<Constructor<?>> annotatedConstructor = Arrays.stream(constructors)
                .filter(constructor ->
                        constructor.isAnnotationPresent(Inject.class) ||
                                constructor.isAnnotationPresent(Autowired.class))
                .findFirst();

        if (annotatedConstructor.isPresent()) {
            return annotatedConstructor.get();
        } else if (constructors.length > 0) {
            return constructors[0];
        } else {
            throw new FXInjectException("No suitable constructor found for class: " + clazz.getName());
        }
    }

    /**
     * Resolves dependencies for constructor parameters.
     * @param constructor The constructor whose parameters need to be resolved
     * @return An array of resolved dependencies
     */
    private Object[] resolveConstructorParameters(Constructor<?> constructor) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] params = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            params[i] = findDependency(paramTypes[i]);
        }

        return params;
    }

    /**
     * Injects dependencies into a component instance.
     * @param instance The component instance to inject dependencies into
     */
    private void injectDependencies(Object instance) {
        Class<?> clazz = instance.getClass();

        Arrays.stream(clazz.getDeclaredFields())
                .filter(field ->
                        field.isAnnotationPresent(Inject.class) ||
                                field.isAnnotationPresent(Autowired.class))
                .forEach(field -> injectField(instance, field));

        Arrays.stream(clazz.getDeclaredMethods())
                .filter(method ->
                        method.isAnnotationPresent(Inject.class) ||
                                method.isAnnotationPresent(Autowired.class))
                .forEach(method -> injectMethod(instance, method));
    }

    /**
     * Injects a dependency into a field.
     * @param instance The component instance
     * @param field The field to inject
     */
    private void injectField(Object instance, Field field) {
        try {
            field.setAccessible(true);
            Object dependency = findDependency(field.getType());
            field.set(instance, dependency);
        } catch (Exception e) {
            logger.error("Failed to inject field: {}", field.getName(), e);
        }
    }

    /**
     * Injects dependencies via a method (e.g., setter method).
     * @param instance The component instance
     * @param method The method to invoke with dependencies
     */
    private void injectMethod(Object instance, Method method) {
        try {
            method.setAccessible(true);
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] params = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = findDependency(paramTypes[i]);
            }

            method.invoke(instance, params);
        } catch (Exception e) {
            logger.error("Failed to inject method: {}", method.getName(), e);
        }
    }

    /**
     * Finds a dependency of a specific type from the registered components.
     * @param type The type of dependency to find
     * @return The found dependency or null if not found
     */
    private Object findDependency(Class<?> type) {
        return components.entrySet().stream()
                .filter(entry -> type.isAssignableFrom(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves a component of a specific type from the container.
     * @param componentClass The class of the component to retrieve
     * @return The component instance or null if not found
     */
    public <T> T getComponent(Class<T> componentClass) {
        Object component = components.get(componentClass);
        if (component == null) {
            logger.warn("No component found for type: {}. Returning null.", componentClass.getName());
            return null;
        }
        return componentClass.cast(component);
    }

    /**
     * Logs a summary of discovered and registered components.
     */
    private void logComponentSummary() {
        logger.info("Dependency Injection Summary:");
        components.keySet().forEach(clazz ->
                logger.info("- Registered Component: {}", clazz.getSimpleName())
        );
        logger.info("Total Components: {}", components.size());
    }
}