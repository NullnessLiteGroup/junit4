package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @deprecated Included for backwards compatibility with JUnit 4.4. Will be
 *             removed in the next major release. Please use
 *             {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.
 */
@Deprecated
public class ClassRoadie {
    private RunNotifier notifier;
    private TestClass testClass;
    private Description description;
    private final Runnable runnable;

    public ClassRoadie(RunNotifier notifier, TestClass testClass,
            Description description, Runnable runnable) {
        this.notifier = notifier;
        this.testClass = testClass;
        this.description = description;
        this.runnable = runnable;
    }

    protected void runUnprotected() {
        runnable.run();
    }

    // Nullable targetException from runAfters()
    protected void addFailure(@Nullable Throwable targetException) {
        notifier.fireTestFailure(new Failure(description, targetException));
    }

    public void runProtected() {
        try {
            runBefores();
            runUnprotected();
        } catch (FailedBefore e) {
        } finally {
            runAfters();
        }
    }

    private void runBefores() throws FailedBefore {
        try {
            try {
                List<Method> befores = testClass.getBefores();
                for (Method before : befores) {
                    before.invoke(null);
                }
            } catch (InvocationTargetException e) {
                // [throwing.nullable] FALSE_POSITIVE
                // 1). runBefores() calls invoke on the method instance to catch
                //     InvocationTargetException, which "wraps an exception thrown by
                //     an invoked method or constructor" documented by the Java 8 API;
                //     @See(https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/InvocationTargetException.html)
                //     but an exception cannot be null, because even if we "throw null;"
                //     from our code, the InvocationTargetException wraps an unchecked
                //     NullPointerException thrown by our method, instead of null;
                // 2). ClassRoadie is not exposed in JUnit4 API,
                //     so users cannot tweak the invoke behavior of the method
                //     instantiated in this class by writing code;
                // 3). It is possible that users can change the binary of the method
                //     to change the runtime behavior, but we seriously doubt whether
                //     users will do that to use JUnit4.
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
        List<Method> afters = testClass.getAfters();
        for (Method after : afters) {
            try {
                after.invoke(null);
            } catch (InvocationTargetException e) {
                addFailure(e.getTargetException());
            } catch (Throwable e) {
                addFailure(e); // Untested, but seems impossible
            }
        }
    }
}
