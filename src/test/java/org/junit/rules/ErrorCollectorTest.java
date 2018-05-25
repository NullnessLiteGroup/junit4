package org.junit.rules;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.junit.rules.EventCollector.*;

@RunWith(Parameterized.class)
public class ErrorCollectorTest {

    @Parameters(name= "{0}")
    public static Object[][] testsWithEventMatcher() {
        return new Object[][]{
                {
                    AddSingleError.class,
                        hasSingleFailureWithMessage("message")},
                {
                    AddTwoErrors.class,
                        hasNumberOfFailures(2)},
                {
                    AddInternalAssumptionViolatedException.class,
                        allOf(hasSingleFailure(), hasNoAssumptionFailure())},
                {
                    CheckMatcherThatDoesNotFailWithoutProvidedReason.class,
                        everyTestRunSuccessful()},
                {
                    CheckMatcherThatDoesNotFailWithProvidedReason.class,
                        everyTestRunSuccessful()},
                {
                    CheckMatcherThatFailsWithoutProvidedReason.class,
                        hasSingleFailureWithMessage(Matchers.<String>allOf(
                            containsString("Expected: is <4>"),
                            containsString("but: was <3>")))},
                {
                    CheckMatcherThatFailsWithProvidedReason.class,
                        hasSingleFailureWithMessage(Matchers.<String>allOf(
                            containsString("reason"),
                            containsString("Expected: is <4>"),
                            containsString("but: was <3>")))},
                {
                    CheckTwoMatchersThatFail.class,
                        hasNumberOfFailures(2)},
                {
                    CheckCallableThatThrowsAnException.class,
                        hasSingleFailureWithMessage("first!")},
                {
                    CheckTwoCallablesThatThrowExceptions.class,
                        hasNumberOfFailures(2)},
                {
                    CheckCallableThatThrowsInternalAssumptionViolatedException.class,
                        allOf(hasSingleFailure(), hasNoAssumptionFailure())},
                {
                    CheckCallableWithFailingAssumption.class,
                        allOf(hasSingleFailure(), hasNoAssumptionFailure())},
                {
                    CheckCallableThatDoesNotThrowAnException.class,
                        everyTestRunSuccessful()},
                {
                    CheckRunnableThatThrowsExpectedTypeOfException.class,
                        everyTestRunSuccessful()},
                {
                    CheckRunnableThatThrowsUnexpectedTypeOfException.class,
                        hasSingleFailureWithMessage("unexpected exception type thrown; expected:<java.lang.IllegalArgumentException> but was:<java.lang.NullPointerException>")},
                {
                    CheckRunnableThatThrowsNoExceptionAlthoughOneIsExpected.class,
                        hasSingleFailureWithMessage("expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown")},
                {
                    ErrorCollectorNotCalledBySuccessfulTest.class,
                        everyTestRunSuccessful()},
                {
                    ErrorCollectorNotCalledByFailingTest.class,
                        hasSingleFailure()},
        };
    }

    @Parameter(0)
    public Class<?> classUnderTest;

    @Parameter(1)
    public Matcher<EventCollector> matcher;

    @Test
    public void runTestClassAndVerifyEvents() {
        EventCollector collector = new EventCollector();
        JUnitCore core = new JUnitCore();
        core.addListener(collector);
        core.run(classUnderTest);
        assertThat(collector, matcher);
    }

    public static class AddSingleError {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.addError(new Throwable("message"));
        }
    }

    public static class AddTwoErrors {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.addError(new Throwable("first thing went wrong"));
            collector.addError(new Throwable("second thing went wrong"));
        }
    }

    public static class AddInternalAssumptionViolatedException {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.addError(new AssumptionViolatedException("message"));
        }
    }

    public static class CheckMatcherThatDoesNotFailWithProvidedReason {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkThat("dummy reason", 3, is(3));
        }
    }

    public static class CheckMatcherThatDoesNotFailWithoutProvidedReason {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkThat(3, is(3));
        }
    }

    public static class CheckMatcherThatFailsWithoutProvidedReason {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkThat(3, is(4));
        }
    }

    public static class CheckMatcherThatFailsWithProvidedReason {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkThat("reason", 3, is(4));
        }
    }

    public static class CheckTwoMatchersThatFail {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkThat(3, is(4));
            collector.checkThat("reason", 7, is(8));
        }
    }

    public static class CheckCallableThatThrowsAnException {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkSucceeds(new Callable<Object>() {
                @NotNull
                public Object call() throws Exception {
                    throw new RuntimeException("first!");
                }
            });
        }
    }

    public static class CheckTwoCallablesThatThrowExceptions {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkSucceeds(new Callable<Object>() {
                @NotNull
                public Object call() throws Exception {
                    throw new RuntimeException("first!");
                }
            });
            collector.checkSucceeds(new Callable<Integer>() {
                @NotNull
                public Integer call() throws Exception {
                    throw new RuntimeException("second!");
                }
            });
        }
    }

    public static class CheckCallableThatThrowsInternalAssumptionViolatedException {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkSucceeds(new Callable<Object>() {
                @NotNull
                public Object call() throws Exception {
                    throw new AssumptionViolatedException("message");
                }
            });
        }
    }

    public static class CheckCallableWithFailingAssumption {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkSucceeds(new Callable<Object>() {
                @Nullable
                public Object call() throws Exception {
                    assumeTrue(false);
                    return null;
                }
            });
        }
    }

    public static class CheckCallableThatDoesNotThrowAnException {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            Object result = collector.checkSucceeds(new Callable<Object>() {
                @NotNull
                public Object call() throws Exception {
                    return 3;
                }
            });
            assertEquals(3, result);
        }
    }

    public static class CheckRunnableThatThrowsExpectedTypeOfException {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
                public void run() throws Throwable {
                    throw new IllegalArgumentException();
                }
            });
        }
    }

    public static class CheckRunnableThatThrowsUnexpectedTypeOfException {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
                public void run() throws Throwable {
                    throw new NullPointerException();
                }
            });
        }
    }

    public static class CheckRunnableThatThrowsNoExceptionAlthoughOneIsExpected {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            collector.checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
                public void run() throws Throwable {
                }
            });
        }
    }

    public static class ErrorCollectorNotCalledBySuccessfulTest {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
        }
    }

    public static class ErrorCollectorNotCalledByFailingTest {
        @NotNull
        @Rule
        public ErrorCollector collector = new ErrorCollector();

        @Test
        public void example() {
            fail();
        }
    }
}
