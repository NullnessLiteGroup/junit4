package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.TestTimedOutException;

/**
 * @deprecated Included for backwards compatibility with JUnit 4.4. Will be
 *             removed in the next major release. Please use
 *             {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.
 */
@Deprecated
public class MethodRoadie {
    private final Object test;
    private final RunNotifier notifier;
    private final Description description;
    private TestMethod testMethod;

    public MethodRoadie(Object test, TestMethod method, RunNotifier notifier, Description description) {
        this.test = test;
        this.notifier = notifier;
        this.description = description;
        testMethod = method;
    }

    public void run() {
        if (testMethod.isIgnored()) {
            notifier.fireTestIgnored(description);
            return;
        }
        notifier.fireTestStarted(description);
        try {
            long timeout = testMethod.getTimeout();
            if (timeout > 0) {
                runWithTimeout(timeout);
            } else {
                runTest();
            }
        } finally {
            notifier.fireTestFinished(description);
        }
    }

    private void runWithTimeout(final long timeout) {
        runBeforesThenTestThenAfters(new Runnable() {

            public void run() {
                @NotNull ExecutorService service = Executors.newSingleThreadExecutor();
                @NotNull Callable<Object> callable = new Callable<Object>() {
                    public Object call() throws Exception {
                        runTestMethod();
                        return null;
                    }
                };
                @NotNull Future<Object> result = service.submit(callable);
                service.shutdown();
                try {
                    boolean terminated = service.awaitTermination(timeout,
                            TimeUnit.MILLISECONDS);
                    if (!terminated) {
                        service.shutdownNow();
                    }
                    result.get(0, TimeUnit.MILLISECONDS); // throws the exception if one occurred during the invocation
                } catch (TimeoutException e) {
                    addFailure(new TestTimedOutException(timeout, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    addFailure(e);
                }
            }
        });
    }

    public void runTest() {
        runBeforesThenTestThenAfters(new Runnable() {
            public void run() {
                runTestMethod();
            }
        });
    }

    public void runBeforesThenTestThenAfters(@NotNull Runnable test) {
        try {
            runBefores();
            test.run();
        } catch (FailedBefore e) {
        } catch (Exception e) {
            throw new RuntimeException("test should never throw an exception to this level");
        } finally {
            runAfters();
        }
    }

    protected void runTestMethod() {
        try {
            testMethod.invoke(test);
            if (testMethod.expectsException()) {
                addFailure(new AssertionError("Expected exception: " + testMethod.getExpectedException().getName()));
                /*
                   [FALSE_POSITIVE]
                   This is a false positve. Let's look at the condition of the above if-branch (line 111):
                   testMethod.expectsException(). By looking at its implementation
                   (src/main/java/org/junit/internal/runners/TestMethod.java: line 61), we know that it returns false
                   if getExpectedException() (TestMethod.java: line 45) returns null.
                   If the error we (line 112) get is as it says, it means testMethod.getExpectedException() returns null.
                   But in this case, testMethod.expectsException() will always be false, meaning that we will never reach
                   the error (line 112). So this is a false positive.
                 */
            }
        } catch (InvocationTargetException e) {
            Throwable actual = e.getTargetException();
            if (actual instanceof AssumptionViolatedException) {
                return;
            } else if (!testMethod.expectsException()) {
                addFailure(actual);
            } else if (testMethod.isUnexpected(actual)) {
                @NotNull String message = "Unexpected exception, expected<" + testMethod.getExpectedException().getName() + "> but was<"
                        + actual.getClass().getName() + ">";
                /*
                   [FALSE_POSITIVE]
                   This is a false positve. Let's look at the condition of the above else-if-branch (line 127):
                   !testMethod.expectsException(). By looking at its implementation
                   (src/main/java/org/junit/internal/runners/TestMethod.java: line 61), we know that it returns false
                   if getExpectedException() (TestMethod.java: line 45) returns null.
                   If the error we get (line 130) is as it says, it means testMethod.getExpectedException() returns null.
                   But in this case, !testMethod.expectsException() will always be true, meaning that we will enter the
                   else-if-branch and never reach this error (ine 130).
                 */
                addFailure(new Exception(message, actual));
            }
        } catch (Throwable e) {
            addFailure(e);
        }
    }

    private void runBefores() throws FailedBefore {
        try {
            try {
                @NotNull List<Method> befores = testMethod.getBefores();
                for (@NotNull Method before : befores) {
                    before.invoke(test);
                }
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        } catch (AssumptionViolatedException e) {
            throw new FailedBefore();
        } catch (Throwable e) {
            addFailure(e);
            throw new FailedBefore();
        }
    }

    private void runAfters() {
        @NotNull List<Method> afters = testMethod.getAfters();
        for (@NotNull Method after : afters) {
            try {
                after.invoke(test);
            } catch (InvocationTargetException e) {
                addFailure(e.getTargetException());
            } catch (Throwable e) {
                addFailure(e); // Untested, but seems impossible
            }
        }
    }

    protected void addFailure(Throwable e) {
        notifier.fireTestFailure(new Failure(description, e));
    }
}

