package org.junit.internal.requests;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.SuiteMethodBuilder;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class ClassRequest extends Request {
    private final Lock runnerLock = new ReentrantLock();

    /*
     * We have to use the f prefix, because IntelliJ's JUnit4IdeaTestRunner uses
     * reflection to access this field. See
     * https://github.com/junit-team/junit4/issues/960
     */
    private final Class<?> fTestClass;
    private final boolean canUseSuiteMethod;
    // Nullable runner from getRunner()
    private volatile @Nullable Runner runner;

    public ClassRequest(Class<?> testClass, boolean canUseSuiteMethod) {
        this.fTestClass = testClass;
        this.canUseSuiteMethod = canUseSuiteMethod;
    }

    public ClassRequest(Class<?> testClass) {
        this(testClass, true);
    }

    @Override
    @SuppressWarnings("nullness")
    public Runner getRunner() {
        if (runner == null) {
            runnerLock.lock();
            try {
                if (runner == null) {
                    runner = new CustomAllDefaultPossibilitiesBuilder().safeRunnerForClass(fTestClass);
                }
            } finally {
                runnerLock.unlock();
            }
        }
        // [return.type.incompatible] FALSE_POSITIVE
        // runner cannot be null here
        // because it is prevented by new CustomAllDefaultPossibilitiesBuilder(),
        // which will never return null runner from safeRunnerForClass(fTestClass);
        return runner;
    }

    private class CustomAllDefaultPossibilitiesBuilder extends AllDefaultPossibilitiesBuilder {

        @Override
        protected RunnerBuilder suiteMethodBuilder() {
            return new CustomSuiteMethodBuilder();
        }
    }

    /*
     * Customization of {@link SuiteMethodBuilder} that prevents use of the
     * suite method when creating a runner for fTestClass when canUseSuiteMethod
     * is false.
     */
    private class CustomSuiteMethodBuilder extends SuiteMethodBuilder {

        @Override
        // Nullable Runner indicated in implementation
        public @Nullable Runner runnerForClass(Class<?> testClass) throws Throwable {
            if (testClass == fTestClass && !canUseSuiteMethod) {
                return null;
            }
            return super.runnerForClass(testClass);
        }
    }
}