package org.junit.internal.builders;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class AllDefaultPossibilitiesBuilder extends RunnerBuilder {
    private final boolean canUseSuiteMethod;

    /**
     * @since 4.13
     */
    public AllDefaultPossibilitiesBuilder() {
        canUseSuiteMethod = true;
    }

    /**
     * @deprecated used {@link #AllDefaultPossibilitiesBuilder()}.
     */
    @Deprecated
    public AllDefaultPossibilitiesBuilder(boolean canUseSuiteMethod) {
        this.canUseSuiteMethod = canUseSuiteMethod;
    }

    @Nullable
    @Override
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        @NotNull List<RunnerBuilder> builders = Arrays.asList(
                ignoredBuilder(),
                annotatedBuilder(),
                suiteMethodBuilder(),
                junit3Builder(),
                junit4Builder());

        for (@NotNull RunnerBuilder each : builders) {
            Runner runner = each.safeRunnerForClass(testClass);
            if (runner != null) {
                return runner;
            }
        }
        return null;
    }

    @NotNull
    protected JUnit4Builder junit4Builder() {
        return new JUnit4Builder();
    }

    @NotNull
    protected JUnit3Builder junit3Builder() {
        return new JUnit3Builder();
    }

    @NotNull
    protected AnnotatedBuilder annotatedBuilder() {
        return new AnnotatedBuilder(this);
    }

    @NotNull
    protected IgnoredBuilder ignoredBuilder() {
        return new IgnoredBuilder();
    }

    protected RunnerBuilder suiteMethodBuilder() {
        if (canUseSuiteMethod) {
            return new SuiteMethodBuilder();
        }
        return new NullBuilder();
    }
}