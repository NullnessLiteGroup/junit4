package org.junit.internal.builders;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class JUnit3Builder extends RunnerBuilder {
    @Override
    // Nullable Runner returned if the passed testClass is not a TestCase (e.g. Object.class)
    public @Nullable Runner runnerForClass(Class<?> testClass) throws Throwable {
        if (isPre4Test(testClass)) {
            return new JUnit38ClassRunner(testClass);
        }
        return null;
    }

    boolean isPre4Test(Class<?> testClass) {
        return junit.framework.TestCase.class.isAssignableFrom(testClass);
    }
}