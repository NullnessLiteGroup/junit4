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
    // Nullable thrownException from the constructor
    protected @Nullable Throwable fThrownException;

    /**
     * Constructs a TestFailure with the given test and exception.
     */
    // Nullable thrownException from TestResult: addError
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
    // Nullable thrownException from the constructor
    public @Nullable Throwable thrownException() {
        return fThrownException;
    }

    /**
     * Returns a short description of the failure.
     */
    @Override
    public String toString() {
        // [dereference.of.nullable] FALSE_POSITIVE
        // Although thrownException can be initialized as null
        // TestFailure isn't exposed in JUnit4 API, so users are
        // unaware of this method; Besides, toString() specified
        // for this TestFailure is never called from this project.
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
    // Nullable String returned from null detailed message of an exception (e.g. FileNotFoundException())
    public @Nullable String exceptionMessage() {
        // [dereference.of.nullable] FALSE_POSITIVE
        // although thrownException() can be null,
        // but NPEs will never be raised here, because
        // TestFailure is not exposed in the JUnit4 API,
        // and the project never calls this method except
        // two calls from the the test, where the thrownException()
        // is ensured to be non-null
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