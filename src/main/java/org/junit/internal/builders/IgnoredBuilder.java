package org.junit.internal.builders;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Ignore;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class IgnoredBuilder extends RunnerBuilder {
    @Override
    // Nullable Runner returned if testClass doesn't have Ignore
    public @Nullable Runner runnerForClass(Class<?> testClass) {
        if (testClass.getAnnotation(Ignore.class) != null) {
            return new IgnoredClassRunner(testClass);
        }
        return null;
    }
}