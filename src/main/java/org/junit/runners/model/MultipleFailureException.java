package org.junit.runners.model;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.TestCouldNotBeSkippedException;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.Throwables;

/**
 * Collects multiple {@code Throwable}s into one exception.
 *
 * @since 4.9
 */
public class MultipleFailureException extends Exception {
    private static final long serialVersionUID = 1L;

    /*
     * We have to use the f prefix until the next major release to ensure
     * serialization compatibility. 
     * See https://github.com/junit-team/junit4/issues/976
     */
    @NotNull
    private final List<Throwable> fErrors;

    public MultipleFailureException(List<Throwable> errors) {
        if (errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "List of Throwables must not be empty");
        }
        this.fErrors = new ArrayList<Throwable>(errors.size());
        for (Throwable error : errors) {
            if (error instanceof AssumptionViolatedException) {
                error = new TestCouldNotBeSkippedException((AssumptionViolatedException) error);
            }
            fErrors.add(error);
        }
    }

    @NotNull
    public List<Throwable> getFailures() {
        return Collections.unmodifiableList(fErrors);
    }

    @NotNull
    @Override
    public String getMessage() {
        @NotNull StringBuilder sb = new StringBuilder(
                String.format("There were %d errors:", fErrors.size()));
        for (@NotNull Throwable e : fErrors) {
            sb.append(String.format("%n  %s(%s)", e.getClass().getName(), e.getMessage()));
        }
        return sb.toString();
    }

    @Override
    public void printStackTrace() {
        for (@NotNull Throwable e: fErrors) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void printStackTrace(PrintStream s) {
        for (@NotNull Throwable e: fErrors) {
            e.printStackTrace(s);
        }
    }
    
    @Override
    public void printStackTrace(PrintWriter s) {
        for (@NotNull Throwable e: fErrors) {
            e.printStackTrace(s);
        }
    }
    
    /**
     * Asserts that a list of throwables is empty. If it isn't empty,
     * will throw {@link MultipleFailureException} (if there are
     * multiple throwables in the list) or the first element in the list
     * (if there is only one element).
     *
     * @param errors list to check
     * @throws Exception or Error if the list is not empty
     */
    @SuppressWarnings("deprecation")
    public static void assertEmpty(List<Throwable> errors) throws Exception {
        if (errors.isEmpty()) {
            return;
        }
        if (errors.size() == 1) {
            throw Throwables.rethrowAsException(errors.get(0));
            /*
               [FALSE_POSITIVE]
               This is a false positive. By looking at the implementation of Throwables.rethrowAsException()
               (src/main/java/org/junit/internal/Throwables.java: line 48),
               we know that it never arrives at line 50 (return null;). Since Throwables.rethrowAsException()
               never returns null, we cannot meet a NullPointerException here.
             */
        }

        /*
           * Many places in the code are documented to throw
           * org.junit.internal.runners.model.MultipleFailureException.
           * That class now extends this one, so we throw the internal
           * exception in case developers have tests that catch
           * MultipleFailureException.
           */
        throw new org.junit.internal.runners.model.MultipleFailureException(errors);
    }
}
