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

import org.checkerframework.checker.nullness.qual.Nullable;
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
                ExecutorService service = Executors.newSingleThreadExecutor();
                Callable<Object> callable = new Callable<Object>() {
                    // Nullable Object returned when after test methods run
                    public @Nullable Object call() throws Exception {
                        runTestMethod();
                        return null;
                    }
                };
                Future<Object> result = service.submit(callable);
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

    public void runBeforesThenTestThenAfters(Runnable test) {
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

    @SuppressWarnings("nullness")
    protected void runTestMethod() {
        try {
            testMethod.invoke(test);
            if (testMethod.expectsException()) {
                // [dereference.of.nullable] FALSE_POSITIVE
                // testMethod.getExpectedException() could not be null
                // because expectsException() ensures it
                addFailure(new AssertionError("Expected exception: " + testMethod.getExpectedException().getName()));
            }
        } catch (InvocationTargetException e) {
            Throwable actual = e.getTargetException();
            if (actual instanceof AssumptionViolatedException) {
                return;
            } else if (!testMethod.expectsException()) {
                addFailure(actual);
            } else if (testMethod.isUnexpected(actual)) {
                // 1) [dereference.of.nullable] FALSE_POSITIVE
                // testMethod.getExpectedException() could not be null
                // because last if statement checks !expectsException() which ensures it
                //
                // 2) [dereference.of.nullable] FALSE_POSITIVE
                // it is possible for actual to be null but NPE is raised
                // in testMethod.isUnexpected(actual)
                String message = "Unexpected exception, expected<" + testMethod.getExpectedException().getName() + "> but was<"
                        + actual.getClass().getName() + ">";
                addFailure(new Exception(message, actual));
            }
        } catch (Throwable e) {
            addFailure(e);
        }
    }

    @SuppressWarnings("nullness")
    private void runBefores() throws FailedBefore {
        try {
            try {
                List<Method> befores = testMethod.getBefores();
                for (Method before : befores) {
                    before.invoke(test);
                }
            } catch (InvocationTargetException e) {
                // [throwing.nullable] TRUE_POSITIVE
                // e.getTargetException() is nullable from the
                // documentation of getCause(), a substitute method
                // since the release of Java 1.4;
                // Although e.getCause() in InvocationTargetException
                // may be intended to be non-null in Java reflection,
                // we decided it JUnit4 needs to be safer.
                // @See Java API that documents getCause can be null
                //      (https://docs.oracle.com/javase/8/docs/api/
                //      java/lang/reflect/InvocationTargetException.html)
                // @See StackOverFlow discussion about when InvocationTargetException
                //      has a null cause (https://stackoverflow.com/questions/
                //      17684484/when-is-invocationtargetexception-getcause-null)
                // @See The blog in Oracle forum discussing the four possibilities of
                //      getCause() (https://blogs.oracle.com/chengfang/
                //      whats-inside-invocationtargetexception-not-just-exception)
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
        List<Method> afters = testMethod.getAfters();
        for (Method after : afters) {
            try {
                after.invoke(test);
            } catch (InvocationTargetException e) {
                addFailure(e.getTargetException());
            } catch (Throwable e) {
                addFailure(e); // Untested, but seems impossible
            }
        }
    }

    // Nullable e from MethodRoadie.runTestMethod()
    protected void addFailure(@Nullable Throwable e) {
        notifier.fireTestFailure(new Failure(description, e));
    }
}

