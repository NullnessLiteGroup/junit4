package org.junit.internal.builders;

import java.util.Arrays;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
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

    @Override
    @SuppressWarnings("nullness")
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        List<RunnerBuilder> builders = Arrays.asList(
                ignoredBuilder(),
                annotatedBuilder(),
                suiteMethodBuilder(),
                junit3Builder(),
                junit4Builder());

        for (RunnerBuilder each : builders) {
            Runner runner = each.safeRunnerForClass(testClass);
            if (runner != null) {
                return runner;
            }
        }
        // [return.type.incompatible] FALSE_POSITIVE
        // we never reach this line,
        // because at least JUnit4Builder().safeRunnerForClass(testClass)
        // will never return null runner;
        // even if it has Initialization error, the
        // safeRunnerForClass will ensure a runner
        // returned for that throwable.
        return null;
    }

    protected JUnit4Builder junit4Builder() {
        return new JUnit4Builder();
    }

    protected JUnit3Builder junit3Builder() {
        return new JUnit3Builder();
    }

    protected AnnotatedBuilder annotatedBuilder() {
        return new AnnotatedBuilder(this);
    }

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