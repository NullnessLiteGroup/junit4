package junit.framework;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.Throwables;


/**
 * A {@code TestFailure} collects a failed test together with
 * the caught exception.
 *
 * @see TestResult
 */
public class TestFailure {
    protected Test fFailedTest;
    protected @Nullable Throwable fThrownException;

    /**
     * Constructs a TestFailure with the given test and exception.
     */
    public TestFailure(Test failedTest, @Nullable Throwable thrownException) {
        fFailedTest = failedTest;
        fThrownException = thrownException;
    }

    /**
     * Gets the failed test.
     */
    public Test failedTest() {
        return fFailedTest;
    }

    /**
     * Gets the thrown exception.
     */
    public @Nullable Throwable thrownException() {
        return fThrownException;
    }

    /**
     * Returns a short description of the failure.
     */
    @Override
    @SuppressWarnings("nullness")
    public String toString() {
        // [dereference.of.nullable] TRUE_POSITIVE
        // fThrownException can be null, toString() is public
        return fFailedTest + ": " + fThrownException.getMessage();
    }
    
    /**
     * Returns a String containing the stack trace of the error
     * thrown by TestFailure.
     */
    public String trace() {
        return Throwables.getStacktrace(thrownException());
    }

    /**
     * Returns a String containing the message from the thrown exception.
     */
    @SuppressWarnings("nullness")
    public @Nullable String exceptionMessage() {
        // [dereference.of.nullable] TRUE_POSITIVE
        // thrownExcption() can return null, this method is public
        return thrownException().getMessage();
    }

    /**
     * Returns {@code true} if the error is considered a failure
     * (i.e. if it is an instance of {@code AssertionFailedError}),
     * {@code false} otherwise.
     */
    public boolean isFailure() {
        return thrownException() instanceof AssertionFailedError;
    }
}