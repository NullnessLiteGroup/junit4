package org.junit.internal.builders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Ignore;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class IgnoredBuilder extends RunnerBuilder {
    @Nullable
    @Override
    public Runner runnerForClass(@NotNull Class<?> testClass) {
        if (testClass.getAnnotation(Ignore.class) != null) {
            return new IgnoredClassRunner(testClass);
        }
        return null;
    }
}