package org.junit.runners.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Runner;

/**
 * A RunnerBuilder is a strategy for constructing runners for classes.
 *
 * Only writers of custom runners should use <code>RunnerBuilder</code>s.  A custom runner class with a constructor taking
 * a <code>RunnerBuilder</code> parameter will be passed the instance of <code>RunnerBuilder</code> used to build that runner itself.
 * For example,
 * imagine a custom runner that builds suites based on a list of classes in a text file:
 *
 * <pre>
 * \@RunWith(TextFileSuite.class)
 * \@SuiteSpecFile("mysuite.txt")
 * class MySuite {}
 * </pre>
 *
 * The implementation of TextFileSuite might include:
 *
 * <pre>
 * public TextFileSuite(Class testClass, RunnerBuilder builder) {
 *   // ...
 *   for (String className : readClassNames())
 *     addRunner(builder.runnerForClass(Class.forName(className)));
 *   // ...
 * }
 * </pre>
 *
 * @see org.junit.runners.Suite
 * @since 4.5
 */
public abstract class RunnerBuilder {
    private final Set<Class<?>> parents = new HashSet<Class<?>>();

    /**
     * Override to calculate the correct runner for a test class at runtime.
     *
     * @param testClass class to be run
     * @return a Runner
     * @throws Throwable if a runner cannot be constructed
     */
    public abstract @Nullable Runner runnerForClass(Class<?> testClass) throws Throwable;

    /**
     * Always returns a runner for the given test class.
     *
     * <p>In case of an exception a runner will be returned that prints an error instead of running
     * tests.
     *
     * <p>Note that some of the internal JUnit implementations of RunnerBuilder will return
     * {@code null} from this method, but no RunnerBuilder passed to a Runner constructor will
     * return {@code null} from this method.
     *
     * @param testClass class to be run
     * @return a Runner
     */
    public @Nullable Runner safeRunnerForClass(Class<?> testClass) {
        try {
            return runnerForClass(testClass);
        } catch (Throwable e) {
            return new ErrorReportingRunner(testClass, e);
        }
    }

    @Nullable Class<?> addParent(@Nullable Class<?> parent) throws InitializationError {
        if (!parents.add(parent)) {
            throw new InitializationError(String.format("class '%s' (possibly indirectly) contains itself as a SuiteClass", parent.getName()));
        }
        return parent;
    }

    void removeParent(@Nullable Class<?> klass) {
        parents.remove(klass);
    }

    /**
     * Constructs and returns a list of Runners, one for each child class in
     * {@code children}.  Care is taken to avoid infinite recursion:
     * this builder will throw an exception if it is requested for another
     * runner for {@code parent} before this call completes.
     */
    public List<Runner> runners(@Nullable Class<?> parent, Class<?>[] children)
            throws InitializationError {
        addParent(parent);

        try {
            return runners(children);
        } finally {
            removeParent(parent);
        }
    }

    public List<Runner> runners(Class<?> parent, List<Class<?>> children)
            throws InitializationError {
        return runners(parent, children.toArray(new Class<?>[0]));
    }

    private List<Runner> runners(Class<?>[] children) {
        List<Runner> runners = new ArrayList<Runner>();
        for (Class<?> each : children) {
            @Nullable Runner childRunner = safeRunnerForClass(each);
            if (childRunner != null) {
                runners.add(childRunner);
            }
        }
        return runners;
    }
}
