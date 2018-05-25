package org.junit.internal.builders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class JUnit3Builder extends RunnerBuilder {
    @Nullable
    @Override
    public Runner runnerForClass(@NotNull Class<?> testClass) throws Throwable {
        if (isPre4Test(testClass)) {
            return new JUnit38ClassRunner(testClass);
        }
        return null;
    }

    boolean isPre4Test(@NotNull Class<?> testClass) {
        return junit.framework.TestCase.class.isAssignableFrom(testClass);
    }
}