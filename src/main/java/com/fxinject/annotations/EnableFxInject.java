package com.fxinject.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable FXInject for a JavaFX application.
 * Apply this annotation to your main Application class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableFxInject {
    /**
     * Specifies the base packages to scan for components.
     * If not provided, FXInject will scan the package of the annotated class.
     * @return An array of package names to scan
     */
    String[] basePackages() default {};
}