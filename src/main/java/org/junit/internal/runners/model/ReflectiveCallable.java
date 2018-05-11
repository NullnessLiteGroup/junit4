package org.junit.internal.runners.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * When invoked, throws the exception from the reflected method, rather than
 * wrapping it in an InvocationTargetException.
 */
@SuppressWarnings("nullness")
public abstract class ReflectiveCallable {
    public @Nullable Object run() throws Throwable {
        try {
            return runReflectiveCall();
        } catch (InvocationTargetException e) {
            // [throwing.nullable] TRUE_POSITIVE
            // the getTargetException has same behavior as getCause,
            // which is documented that can return null
            throw e.getTargetException();
        }
    }

    protected abstract @Nullable Object runReflectiveCall() throws Throwable;
}
