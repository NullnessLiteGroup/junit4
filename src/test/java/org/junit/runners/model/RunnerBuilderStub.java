package org.junit.runners.model;

import org.jetbrains.annotations.NotNull;
import org.junit.runner.Runner;
import org.junit.runner.RunnerSpy;

public class RunnerBuilderStub extends RunnerBuilder {
    @NotNull
    @Override
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        return new RunnerSpy(testClass, this);
    }
}