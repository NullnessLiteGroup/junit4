package org.junit.internal.runners.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * When invoked, throws the exception from the reflected method, rather than
 * wrapping it in an InvocationTargetException.
 */
public abstract class ReflectiveCallable {
    // Nullable Object returned from runReflectiveCall()
    public @Nullable Object run() throws Throwable {
        try {
            return runReflectiveCall();
        } catch (InvocationTargetException e) {
            // [throwing.nullable] FALSE_POSITIVE
            // 1). ReflectiveCallable is not exposed in JUnit4 API,
            //     and the two anonymous subclasses in this project both
            //     use java reflection to catch the exception from the
            //     reflected method; Such exception wrapped in InvocationTargetException
            //     cannot be null, and users cannot change the behavior of java reflection
            //     by code. Here are the two callers:
            //          a. FrameworkMethod: invokeExplosively(final Object target, final Object... params)
            //          b. BlockJUnit4ClassRunner: methodBlock(final FrameworkMethod method)
            // 2). Although it is possible that users can change the binary of the method
            //     to change the runtime behavior, we seriously doubt whether
            //     users will do that to use JUnit4.
            throw e.getTargetException();
        }
    }

    // Nullable Object returned from FrameworkMethod.invokeExplosively(final Object target, final Object... params)
    protected abstract @Nullable Object runReflectiveCall() throws Throwable;
}