package org.junit.internal.builders;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class NullBuilder extends RunnerBuilder {
    @Override
    // @Nullable Runner returned from return null
    public @Nullable Runner runnerForClass(Class<?> each) throws Throwable {
        return null;
    }
}