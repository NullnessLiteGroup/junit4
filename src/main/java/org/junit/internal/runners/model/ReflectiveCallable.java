package org.junit.internal.runners.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * When invoked, throws the exception from the reflected method, rather than
 * wrapping it in an InvocationTargetException.
 */
public abstract class ReflectiveCallable {
    public Object run() throws Throwable {
        try {
            return runReflectiveCall();
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    // Nullable Object returned from FrameworkMethod.invokeExplosively(final Object target, final Object... params)
    protected abstract @Nullable Object runReflectiveCall() throws Throwable;
}