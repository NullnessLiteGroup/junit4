package org.junit.internal.builders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class SuiteMethodBuilder extends RunnerBuilder {
    @Nullable
    @Override
    public Runner runnerForClass(@NotNull Class<?> each) throws Throwable {
        if (hasSuiteMethod(each)) {
            return new SuiteMethod(each);
        }
        return null;
    }

    public boolean hasSuiteMethod(@NotNull Class<?> testClass) {
        try {
            testClass.getMethod("suite");
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }
}