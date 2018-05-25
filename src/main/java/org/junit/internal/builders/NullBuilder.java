package org.junit.internal.builders;

import org.jetbrains.annotations.Nullable;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class NullBuilder extends RunnerBuilder {
    @Nullable
    @Override
    public Runner runnerForClass(Class<?> each) throws Throwable {
        return null;
    }
}