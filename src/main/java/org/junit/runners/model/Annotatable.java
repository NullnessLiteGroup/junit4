package org.junit.runners.model;

import org.jetbrains.annotations.Nullable;

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
    @Nullable
    <T extends Annotation> T getAnnotation(Class<T> annotationType);
}
