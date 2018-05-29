package org.junit.experimental.max;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestSuite;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.requests.SortingRequest;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

/**
 * A replacement for JUnitCore, which keeps track of runtime and failure history, and reorders tests
 * to maximize the chances that a failing test occurs early in the test run.
 *
 * The rules for sorting are:
 * <ol>
 * <li> Never-run tests first, in arbitrary order
 * <li> Group remaining tests by the date at which they most recently failed.
 * <li> Sort groups such that the most recent failure date is first, and never-failing tests are at the end.
 * <li> Within a group, run the fastest tests first.
 * </ol>
 */
public class MaxCore {
    private static final String MALFORMED_JUNIT_3_TEST_CLASS_PREFIX = "malformed JUnit 3 test class: ";

    /**
     * Create a new MaxCore from a serialized file stored at storedResults
     *
     * @deprecated use storedLocally()
     */
    @Deprecated
    public static MaxCore forFolder(String folderName) {
        return storedLocally(new File(folderName));
    }

    /**
     * Create a new MaxCore from a serialized file stored at storedResults
     */
    public static MaxCore storedLocally(File storedResults) {
        return new MaxCore(storedResults);
    }

    private final MaxHistory history;

    private MaxCore(File storedResults) {
        history = MaxHistory.forFolder(storedResults);
    }

    /**
     * Run all the tests in <code>class</code>.
     *
     * @return a {@link Result} describing the details of the test run and the failed tests.
     */
    public Result run(Class<?> testClass) {
        return run(Request.aClass(testClass));
    }

    /**
     * Run all the tests contained in <code>request</code>.
     *
     * @param request the request describing tests
     * @return a {@link Result} describing the details of the test run and the failed tests.
     */
    public Result run(Request request) {
        return run(request, new JUnitCore());
    }

    /**
     * Run all the tests contained in <code>request</code>.
     *
     * This variant should be used if {@code core} has attached listeners that this
     * run should notify.
     *
     * @param request the request describing tests
     * @param core a JUnitCore to delegate to.
     * @return a {@link Result} describing the details of the test run and the failed tests.
     */
    public Result run(Request request, JUnitCore core) {
        core.addListener(history.listener());
        return core.run(sortRequest(request).getRunner());
    }

    /**
     * @return a new Request, which contains all of the same tests, but in a new order.
     */
    public Request sortRequest(Request request) {
        if (request instanceof SortingRequest) {
            // We'll pay big karma points for this
            return request;
        }
        List<Description> leaves = findLeaves(request);
        Collections.sort(leaves, history.testComparator());
        return constructLeafRequest(leaves);
    }

    private Request constructLeafRequest(List<Description> leaves) {
        final List<Runner> runners = new ArrayList<Runner>();
        for (Description each : leaves) {
            runners.add(buildRunner(each));
        }
        return new Request() {
            @Override
            public Runner getRunner() {
                try {
                    return new Suite((Class<?>) null, runners) {
                    };
                } catch (InitializationError e) {
                    return new ErrorReportingRunner(null, e);
                }
            }
        };
    }

    private Runner buildRunner(Description each) {
        if (each.toString().equals("TestSuite with 0 tests")) {
            return Suite.emptySuite();
        }
        if (each.toString().startsWith(MALFORMED_JUNIT_3_TEST_CLASS_PREFIX)) {
            // This is cheating, because it runs the whole class
            // to get the warning for this method, but we can't do better,
            // because JUnit 3.8's
            // thrown away which method the warning is for.
            return new JUnit38ClassRunner(new TestSuite(getMalformedTestClass(each)));
        }
        Class<?> type = each.getTestClass();
        if (type == null) {
            throw new RuntimeException("Can't build a runner from description [" + each + "]");
        }
        String methodName = each.getMethodName();
        if (methodName == null) {
            // [return.type.incompatible] FALSE_POSITIVE
            // Request.aClass(type).getRunner() cannot be null here
            // because it eventually called ClassRequest(type).getRunner(),
            // which uses CustomAllDefaultPossibilitiesBuilder to avoid null runner;
            // CustomAllDefaultPossibilitiesBuilder extends from
            // AllDefaultPossibilitiesBuilder, which never returns a null Runner;
            return Request.aClass(type).getRunner();
        }
        // [return.type.incompatible] FALSE_POSITIVE
        // Request.method(type, methodName).getRunner() cannot be null here
        // because it eventually called ClassRequest(type).getRunner() from
        // FilterRequest().getRunner(),
        // and uses CustomAllDefaultPossibilitiesBuilder to avoid null runner;
        // CustomAllDefaultPossibilitiesBuilder extends from
        // AllDefaultPossibilitiesBuilder, which never returns a null Runner;
        return Request.method(type, methodName).getRunner();
    }

    // Nullable Class<?> returned if throw ClassNotFoundException
    private Class<?> getMalformedTestClass(Description each) {
        try {
            return Class.forName(each.toString().replace(MALFORMED_JUNIT_3_TEST_CLASS_PREFIX, ""));
        } catch (ClassNotFoundException e) {
            // [return.type.incompatible] FALSE_POSITIVE
            // we never reach this line
            // cheating methods if user test is in JUnit3 style
            // keyword MALFORMED_JUNIT_3_TEST_CLASS_PREFIX is not exposed to users
            // we can see MALFORMED_JUNIT_3_TEST_CLASS_PREFIX is only used in
            // MaxCore: findLeaves when we get description: "warning(junit.framework.TestSuite$1)"
            // so the class exists and is already examined by the JUnit4 runner.
            return null;
        }
    }

    /**
     * @param request a request to run
     * @return a list of method-level tests to run, sorted in the order
     *         specified in the class comment.
     */
    public List<Description> sortedLeavesForTest(Request request) {
        return findLeaves(sortRequest(request));
    }

    private List<Description> findLeaves(Request request) {
        List<Description> results = new ArrayList<Description>();
        // [dereference.of.nullable] TRUE_POSITIVE
        // dereference of request.getRunner() is unsafe here
        // because JUnit4 API doesn't prevent users from calling:
        // MaxCore.storedLocally(new File("/* some path */")).sortRequest(Request.runner(null));
        findLeaves(null, request.getRunner().getDescription(), results);
        return results;
    }

    // Nullable parent from MaxCore: findLeaves(Request request)
    private void findLeaves(@Nullable Description parent, Description description, List<Description> results) {
        if (description.getChildren().isEmpty()) {
            if (description.toString().equals("warning(junit.framework.TestSuite$1)")) {
                results.add(Description.createSuiteDescription(MALFORMED_JUNIT_3_TEST_CLASS_PREFIX + parent));
            } else {
                results.add(description);
            }
        } else {
            for (Description each : description.getChildren()) {
                findLeaves(description, each, results);
            }
        }
    }
}