package junit.framework;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A Listener for test progress
 */
public interface TestListener {
    /**
     * An error occurred.
     */
    // Nullable e from TestReuslt: addError
    public void addError(Test test, @Nullable Throwable e);

    /**
     * A failure occurred.
     */
    public void addFailure(Test test, AssertionFailedError e);

    /**
     * A test ended.
     */
    public void endTest(Test test);

    /**
     * A test started.
     */
    public void startTest(Test test);
}