package org.junit.runners.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;

/**
 * A model element that may have annotations.
 * 
 * @since 4.12
 */
public interface Annotatable {
    /**
     * Returns the model elements' annotations.
     */
    Annotation[] getAnnotations();

    /**
     * Returns the annotation on the model element of the given type, or @code{null}
     */
    // @Nullable T returned by documentation above
    <T extends Annotation> @Nullable T getAnnotation(Class<T> annotationType);
}
