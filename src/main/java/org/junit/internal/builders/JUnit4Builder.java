package org.junit.internal.builders;

import org.jetbrains.annotations.NotNull;
import org.junit.runner.Runner;
import org.junit.runners.JUnit4;
import org.junit.runners.model.RunnerBuilder;

public class JUnit4Builder extends RunnerBuilder {
    @NotNull
    @Override
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        return new JUnit4(testClass);
    }
}
