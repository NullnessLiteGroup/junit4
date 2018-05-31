package org.junit.runners.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Represents a field on a test class (currently used only for Rules in
 * {@link BlockJUnit4ClassRunner}, but custom runners can make other uses)
 *
 * @since 4.7
 */
public class FrameworkField extends FrameworkMember<FrameworkField> {
    @Nullable
    private final Field field;

    FrameworkField(@Nullable Field field) {
        if (field == null) {
            throw new NullPointerException(
                    "FrameworkField cannot be created without an underlying field.");
        }
        this.field = field;

        if (isPublic()) {
            // This field could be a public field in a package-scope base class
            try {
                field.setAccessible(true);
            } catch (SecurityException  e) {
                // We may get an IllegalAccessException when we try to access the field
            }
        }
    }

    @Override
    public String getName() {
        return getField().getName();
        /*
           [FALSE_POSITIVE]
           This is a false positive. By looking at the implementation of
           getField() (line 105),
           we get to know that it returns the field called "field" which might be null.
           However, the constructor (line 21) checks its parameter: if the passing
           parameter is null, it throws an exception; otherwise, it assigns the parameter
           to the field "field". This means without an exception, field won't
           be null. And therefore, getField() won't return null and getField().getName()
           won't cause a NullPointerException.
         */
    }

    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }
    /*
       [FALSE_POSITIVE]
       This is a false positive. By looking at the constructor (line 21),
       we get to know that it first checks its parameter: if the passing
       parameter is null, it throws an exception; otherwise, it assigns the parameter
       to the field "field". This means without an exception, field will never
       be null. And therefore, field.getAnnotations() won't cause a NullPointerException.
     */

    public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationType) {
        return field.getAnnotation(annotationType);
        /*
           [FALSE_POSITIVE]
           This is a false positive. By looking at the constructor (line 21),
           we get to know that it first checks its parameter: if the passing
           parameter is null, it throws an exception; otherwise, it assigns the parameter
           to the field "field". This means without an exception, field will never
           be null. And therefore, field.getAnnotations() won't cause a NullPointerException.
         */
    }

    @Override
    public boolean isShadowedBy(@NotNull FrameworkField otherMember) {
        return isStatic() && otherMember.getName().equals(getName());
    }

    @Override
    boolean isBridgeMethod() {
        return false;
    }

    @Override
    protected int getModifiers() {
        return field.getModifiers();
    }
    /*
       [FALSE_POSITIVE]
       This is a false positive. By looking at the constructor (line 21),
       we get to know that it first checks its parameter: if the passing
       parameter is null, it throws an exception; otherwise, it assigns the parameter
       to the field "field". This means without an exception, field will never
       be null. And therefore, field.getModifiers() won't cause a NullPointerException.
     */

    /**
     * @return the underlying java Field
     */
    @Nullable
    public Field getField() {
        return field;
    }

    /**
     * @return the underlying Java Field type
     * @see java.lang.reflect.Field#getType()
     */
    @Override
    public Class<?> getType() {
        return field.getType();
    }
    /*
       [FALSE_POSITIVE]
       This is a false positive. By looking at the constructor (line 21),
       we get to know that it first checks its parameter: if the passing
       parameter is null, it throws an exception; otherwise, it assigns the parameter
       to the field "field". This means without an exception, field will never
       be null. And therefore, field.getType() won't cause a NullPointerException.
     */
    
    @Override
    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }
    /*
       [FALSE_POSITIVE]
       This is a false positive. By looking at the constructor (line 21),
       we get to know that it first checks its parameter: if the passing
       parameter is null, it throws an exception; otherwise, it assigns the parameter
       to the field "field". This means without an exception, field will never
       be null. And therefore, field.getDeclaringClass() won't cause a NullPointerException.
     */

    /**
     * Attempts to retrieve the value of this field on {@code target}
     */
    public Object get(Object target) throws IllegalArgumentException, IllegalAccessException {
        return field.get(target);
        /*
           [FALSE_POSITIVE]
           This is a false positive. By looking at the constructor (line 21),
           we get to know that it first checks its parameter: if the passing
           parameter is null, it throws an exception; otherwise, it assigns the parameter
           to the field "field". This means without an exception, field will never
           be null. And therefore, field.get() won't cause a NullPointerException.
         */
    }

    @NotNull
    @Override
    public String toString() {
        return field.toString();
    }
    /*
       [FALSE_POSITIVE]
       This is a false positive. By looking at the constructor (line 21),
       we get to know that it first checks its parameter: if the passing
       parameter is null, it throws an exception; otherwise, it assigns the parameter
       to the field "field". This means without an exception, field will never
       be null. And therefore, field.toString() won't cause a NullPointerException.
     */
}
