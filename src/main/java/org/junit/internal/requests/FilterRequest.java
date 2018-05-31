package org.junit.internal.requests;

import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

/**
 * A filtered {@link Request}.
 */
public final class FilterRequest extends Request {
    private final Request request;
    /*
     * We have to use the f prefix, because IntelliJ's JUnit4IdeaTestRunner uses
     * reflection to access this field. See
     * https://github.com/junit-team/junit4/issues/960
     */
    private final Filter fFilter;

    /**
     * Creates a filtered Request
     *
     * @param request a {@link Request} describing your Tests
     * @param filter {@link Filter} to apply to the Tests described in
     * <code>request</code>
     */
    public FilterRequest(Request request, Filter filter) {
        this.request = request;
        this.fFilter = filter;
    }

    @Override
    @SuppressWarnings("nullness")
    public Runner getRunner() {
        try {
            Runner runner = request.getRunner();
            fFilter.apply(runner);
            // [return.type.incompatible] FALSE_POSITIVE
            // runner cannot be null here, because FilterRequest
            // is not exposed in JUnit4 API, and the constructor
            // is only called from JUnitCommandLineParseResult: createRequest()
            // and Request: method(); Both of them ensures the underlying
            // runner returns non-null runner
            return runner;
        } catch (NoTestsRemainException e) {
            return new ErrorReportingRunner(Filter.class, new Exception(String
                    .format("No tests found matching %s from %s", fFilter
                            .describe(), request.toString())));
        }
    }
}