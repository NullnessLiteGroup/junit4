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
            // [throwing.nullable] TRUE_POSITIVE
            // e.getTargetException() is nullable from the
            // documentation of getCause(), a substitute method
            // since the release of Java 1.4;
            // Although e.getCause() in InvocationTargetException
            // may be intended to be non-null in Java reflection,
            // we decided it JUnit4 needs to be safer.
            // @See Java API that documents getCause can be null
            //      (https://docs.oracle.com/javase/8/docs/api/
            //      java/lang/reflect/InvocationTargetException.html)
            // @See StackOverFlow discussion about when InvocationTargetException
            //      has a null cause (https://stackoverflow.com/questions/
            //      17684484/when-is-invocationtargetexception-getcause-null)
            // @See The blog in Oracle forum discussing the four possibilities of
            //      getCause() (https://blogs.oracle.com/chengfang/
            //      whats-inside-invocationtargetexception-not-just-exception)
            throw e.getTargetException();
        }
    }

    // Nullable Object returned from FrameworkMethod.invokeExplosively(final Object target, final Object... params)
    protected abstract @Nullable Object runReflectiveCall() throws Throwable;
}