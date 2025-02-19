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
 *
 * Key Features:
 * - Automatically scan and discover components in packages
 * - Instantiate components with dependency injection
 * - Support for constructor, method, and field injection
 * - Supports both @Inject and @Autowired annotations
 *
 * Usage:
 * FXInjectContainer container = new FXInjectContainer();
 * container.scan(); // Automatically scan caller's package
 * YourService service = container.getComponent(YourService.class);
 */
public class FXInjectContainer {
    private static final Logger logger = LoggerFactory.getLogger(FXInjectContainer.class);

    // Stores instantiated components
    private final Map<Class<?>, Object> components = new ConcurrentHashMap<>();

    /**
     * Automatically scans for components starting from the caller's package.
     */
    public void scan() {
        // Get the calling class
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callerClassName = stackTrace[2].getClassName();

        try {
            Class<?> callerClass = Class.forName(callerClassName);
            String basePackage = callerClass.getPackage().getName();

            // Scan from the base package of the caller
            scan(basePackage);
        } catch (ClassNotFoundException e) {
            logger.error("Could not determine caller's package", e);
            throw new FXInjectException("Automatic package scanning failed", e);
        }
    }

    /**
     * Scans the specified base package for components and sets up dependency injection.
     *
     * Process:
     * 1. Find all classes annotated with @Component
     * 2. Instantiate components
     * 3. Inject dependencies into instantiated components
     *
     * @param basePackage The root package to scan for components
     */
    public void scan(String basePackage) {
        // Discover component classes in the specified package
        Set<Class<?>> componentClasses = ClassPathScanner.findComponentClasses(basePackage);

        // First pass: create instances of all discovered components
        componentClasses.forEach(this::instantiateComponent);

        // Second pass: inject dependencies into created components
        components.values().forEach(this::injectDependencies);

        // Log components summary
        logComponentSummary();
    }

    /**
     * Creates an instance of a component class.
     *
     * Tries to:
     * - Find an injectable constructor (with @Inject or @Autowired)
     * - Resolve constructor parameters
     * - Create an instance of the component
     *
     * @param componentClass The class to instantiate
     */
    private void instantiateComponent(Class<?> componentClass) {
        try {
            // Find the most appropriate constructor for instantiation
            Constructor<?> constructor = findInjectableConstructor(componentClass);
            constructor.setAccessible(true);

            // Resolve and prepare constructor parameters
            Object[] params = resolveConstructorParameters(constructor);

            // Create an instance of the component
            Object instance = constructor.newInstance(params);

            // Store the created instance
            components.put(componentClass, instance);
        } catch (Exception e) {
            logger.error("Failed to instantiate component: {}", componentClass.getName(), e);
            throw new FXInjectException("Failed to instantiate component: " + componentClass.getName(), e);
        }
    }

    /**
     * Finds the most appropriate constructor for component instantiation.
     *
     * Prioritization:
     * 1. Constructor with @Inject or @Autowired annotation
     * 2. Default no-arg constructor
     * 3. First available constructor
     *
     * @param clazz The class to find a constructor for
     * @return The selected constructor
     */
    private Constructor<?> findInjectableConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();

        // Look for constructors with injection annotations
        Optional<Constructor<?>> annotatedConstructor = Arrays.stream(constructors)
                .filter(constructor ->
                        constructor.isAnnotationPresent(Inject.class) ||
                                constructor.isAnnotationPresent(Autowired.class))
                .findFirst();

        // Fallback to default constructor or first available
        return annotatedConstructor.orElseGet(() -> {
            try {
                return clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                // If no default constructor, use first constructor
                return clazz.getConstructors()[0];
            }
        });
    }

    /**
     * Resolves dependencies for constructor parameters.
     *
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
     *
     * Supports:
     * - Field injection
     * - Method injection
     *
     * @param instance The component instance to inject dependencies into
     */
    private void injectDependencies(Object instance) {
        Class<?> clazz = instance.getClass();

        // Inject dependencies into fields
        Arrays.stream(clazz.getDeclaredFields())
                .filter(field ->
                        field.isAnnotationPresent(Inject.class) ||
                                field.isAnnotationPresent(Autowired.class))
                .forEach(field -> injectField(instance, field));

        // Inject dependencies via setter methods
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(method ->
                        method.isAnnotationPresent(Inject.class) ||
                                method.isAnnotationPresent(Autowired.class))
                .forEach(method -> injectMethod(instance, method));
    }

    /**
     * Injects a dependency into a field.
     *
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
            throw new FXInjectException("Failed to inject field: " + field.getName(), e);
        }
    }

    /**
     * Injects dependencies via a method (e.g., setter method).
     *
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
            throw new FXInjectException("Failed to inject method: " + method.getName(), e);
        }
    }

    /**
     * Finds a dependency of a specific type from the registered components.
     *
     * Supports:
     * - Exact type match
     * - Subtype and interface matching
     *
     * @param type The type of dependency to find
     * @return The found dependency
     * @throws FXInjectException if no suitable dependency is found
     */
    private Object findDependency(Class<?> type) {
        return components.entrySet().stream()
                .filter(entry -> type.isAssignableFrom(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new FXInjectException("No dependency found for type: " + type.getName()));
    }

    /**
     * Retrieves a component of a specific type from the container.
     *
     * @param componentClass The class of the component to retrieve
     * @return The component instance
     * @throws FXInjectException if no component is found
     */
    public <T> T getComponent(Class<T> componentClass) {
        Object component = components.get(componentClass);
        if (component == null) {
            throw new FXInjectException("No component found for type: " + componentClass.getName());
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