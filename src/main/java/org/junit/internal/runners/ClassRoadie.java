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
