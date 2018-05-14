package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @deprecated Included for backwards compatibility with JUnit 4.4. Will be
 *             removed in the next major release. Please use
 *             {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.
 */
@Deprecated
public class TestMethod {
    private final Method method;
    private TestClass testClass;

    public TestMethod(Method method, TestClass testClass) {
        this.method = method;
        this.testClass = testClass;
    }

    public boolean isIgnored() {
        return method.getAnnotation(Ignore.class) != null;
    }

    public long getTimeout() {
        Test annotation = method.getAnnotation(Test.class);
        if (annotation == null) {
            return 0;
        }
        long timeout = annotation.timeout();
        return timeout;
    }

    // Nullable Class<? extends Throwable> returned indicated below
    protected @Nullable Class<? extends Throwable> getExpectedException() {
        Test annotation = method.getAnnotation(Test.class);
        if (annotation == null || annotation.expected() == None.class) {
            return null;
        } else {
            return annotation.expected();
        }
    }

    // Nullable exception from MethodRoadie.runTestMethod()
    boolean isUnexpected(@Nullable Throwable exception) {
        // [dereference.of.nullable] TRUE_POSITIVE
        // dereference of possibly-null reference getExpectedException()
        // [dereference.of.nullable] TRUE_POSITIVE
        // dereference of possibly-null reference exception
        return !getExpectedException().isAssignableFrom(exception.getClass());
    }

    boolean expectsException() {
        return getExpectedException() != null;
    }

    List<Method> getBefores() {
        return testClass.getAnnotatedMethods(Before.class);
    }

    List<Method> getAfters() {
        return testClass.getAnnotatedMethods(After.class);
    }

    public void invoke(Object test) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        method.invoke(test);
    }

}
