package com.fxinject.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// This is just an alias for @Inject
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Autowired {
    /**
     * Indicates whether the dependency is required.
     * If true, a suitable bean must exist, otherwise throw an exception.
     */
    boolean required() default true;

    /**
     * Optional qualifier for more precise dependency injection
     */
    String value() default "";
}