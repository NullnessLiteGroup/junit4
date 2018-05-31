package junit.textui;


import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;
import junit.runner.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command line based tool to run tests.
 * <pre>
 * java junit.textui.TestRunner [-wait] TestCaseClass
 * </pre>
 * <p>
 * TestRunner expects the name of a TestCase class as argument.
 * If this class defines a static <code>suite</code> method it
 * will be invoked and the returned test is run. Otherwise all
 * the methods starting with "test" having no arguments are run.
 * <p>
 * When the wait command line argument is given TestRunner
 * waits until the users types RETURN.
 * <p>
 * TestRunner prints a trace as the tests are executed followed by a
 * summary at the end.
 */
public class TestRunner extends BaseTestRunner {
    private ResultPrinter fPrinter;

    public static final int SUCCESS_EXIT = 0;
    public static final int FAILURE_EXIT = 1;
    public static final int EXCEPTION_EXIT = 2;

    /**
     * Constructs a TestRunner.
     */
    public TestRunner() {
        this(System.out);
    }

    /**
     * Constructs a TestRunner using the given stream for all the output
     */
    public TestRunner(PrintStream writer) {
        this(new ResultPrinter(writer));
    }

    /**
     * Constructs a TestRunner using the given ResultPrinter all the output
     */
    public TestRunner(ResultPrinter printer) {
        fPrinter = printer;
    }

    /**
     * Runs a suite extracted from a TestCase subclass.
     */
    static public void run(Class<? extends TestCase> testClass) {
        run(new TestSuite(testClass));
    }

    /**
     * Runs a single test and collects its results.
     * This method can be used to start a test run
     * from your program.
     * <pre>
     * public static void main (String[] args) {
     *    test.textui.TestRunner.run(suite());
     * }
     * </pre>
     */
    @NotNull
    static public TestResult run(@NotNull Test test) {
        @NotNull TestRunner runner = new TestRunner();
        return runner.doRun(test);
    }

    /**
     * Runs a single test and waits until the user
     * types RETURN.
     */
    static public void runAndWait(@NotNull Test suite) {
        @NotNull TestRunner aTestRunner = new TestRunner();
        aTestRunner.doRun(suite, true);
    }

    @Override
    public void testFailed(int status, Test test, Throwable e) {
    }

    @Override
    public void testStarted(String testName) {
    }

    @Override
    public void testEnded(String testName) {
    }

    /**
     * Creates the TestResult to be used for the test run.
     */
    @NotNull
    protected TestResult createTestResult() {
        return new TestResult();
    }

    @NotNull
    public TestResult doRun(@NotNull Test test) {
        return doRun(test, false);
    }

    @NotNull
    public TestResult doRun(@NotNull Test suite, boolean wait) {
        @NotNull TestResult result = createTestResult();
        result.addListener(fPrinter);
        long startTime = System.currentTimeMillis();
        suite.run(result);
        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;
        fPrinter.print(result, runTime);

        pause(wait);
        return result;
    }

    protected void pause(boolean wait) {
        if (!wait) return;
        fPrinter.printWaitPrompt();
        try {
            System.in.read();
        } catch (Exception e) {
        }
    }

    public static void main(@NotNull String[] args) {
        @NotNull TestRunner aTestRunner = new TestRunner();
        try {
            @NotNull TestResult r = aTestRunner.start(args);
            if (!r.wasSuccessful()) {
                System.exit(FAILURE_EXIT);
            }
            System.exit(SUCCESS_EXIT);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(EXCEPTION_EXIT);
        }
    }

    /**
     * Starts a test run. Analyzes the command line arguments and runs the given
     * test suite.
     */
    @NotNull
    public TestResult start(@NotNull String[] args) throws Exception {
        String testCase = "";
        @NotNull String method = "";
        boolean wait = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-wait")) {
                wait = true;
            } else if (args[i].equals("-c")) {
                testCase = extractClassName(args[++i]);
            } else if (args[i].equals("-m")) {
                String arg = args[++i];
                int lastIndex = arg.lastIndexOf('.');
                testCase = arg.substring(0, lastIndex);
                method = arg.substring(lastIndex + 1);
            } else if (args[i].equals("-v")) {
                System.err.println("JUnit " + Version.id() + " by Kent Beck and Erich Gamma");
            } else {
                testCase = args[i];
            }
        }

        if (testCase.equals("")) {
            throw new Exception("Usage: TestRunner [-wait] testCaseName, where name is the name of the TestCase class");
        }

        try {
            if (!method.equals("")) {
                return runSingleMethod(testCase, method, wait);
            }
            @Nullable Test suite = getTest(testCase);
            return doRun(suite, wait);
            /*
               [FALSE_POSITIVE]
               This is a false positive.
               Because the JUnit wiki (https://github.com/junit-team/junit4/wiki/test-runners)
               and JUnit4 API doesn't introduce TestRunner and its usage.
               Besides, the internal calls to this method in the project
               will never get this line
             */
        } catch (Exception e) {
            throw new Exception("Could not create and run test suite: " + e);
        }
    }

    @NotNull
    protected TestResult runSingleMethod(String testCase, String method, boolean wait) throws Exception {
        @NotNull Class<? extends TestCase> testClass = loadSuiteClass(testCase).asSubclass(TestCase.class);
        Test test = TestSuite.createTest(testClass, method);
        return doRun(test, wait);
    }

    @Override
    protected void runFailed(String message) {
        System.err.println(message);
        System.exit(FAILURE_EXIT);
    }

    public void setPrinter(ResultPrinter printer) {
        fPrinter = printer;
    }


}