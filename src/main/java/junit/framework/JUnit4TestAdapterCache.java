package junit.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class JUnit4TestAdapterCache extends HashMap<Description, Test> {
    private static final long serialVersionUID = 1L;
    private static final JUnit4TestAdapterCache fInstance = new JUnit4TestAdapterCache();

    @NotNull
    public static JUnit4TestAdapterCache getDefault() {
        return fInstance;
    }

    @Nullable
    public Test asTest(@NotNull Description description) {
        if (description.isSuite()) {
            return createTest(description);
        } else {
            if (!containsKey(description)) {
                put(description, createTest(description));
            }
            return get(description);
        }
    }

    @Nullable
    Test createTest(@NotNull Description description) {
        if (description.isTest()) {
            return new JUnit4TestCaseFacade(description);
        } else {
            @Nullable TestSuite suite = new TestSuite(description.getDisplayName());
            for (@NotNull Description child : description.getChildren()) {
                suite.addTest(asTest(child));
            }
            return suite;
        }
    }

    @NotNull
    public RunNotifier getNotifier(@NotNull final TestResult result, final JUnit4TestAdapter adapter) {
        @NotNull RunNotifier notifier = new RunNotifier();
        notifier.addListener(new RunListener() {
            @Override
            public void testFailure(@NotNull Failure failure) throws Exception {
                result.addError(asTest(failure.getDescription()), failure.getException());
            }

            @Override
            public void testFinished(@NotNull Description description) throws Exception {
                result.endTest(asTest(description));
            }

            @Override
            public void testStarted(@NotNull Description description) throws Exception {
                result.startTest(asTest(description));
                /*
                  This is a true positive because asTest(description) might return null, which violates the contract
                  that startsTest() requires a NotNull parameter. By looking at the implementation of asTest(), we
                  get to know that it might return null: in the else-branch, it "return get(description);"
                  (line 32). get() is a method of HashMap and if the given parameter (description) is not in the key set,
                  it returns null.
                  However, if we change the annotation of the parameter of startsTest() to Nullable, there will be a potential
                  NullPointerException when test.countTestCases() is called (src/main/java/junit/framework/TestResult.java: line 169).
                  And thus we cannot change the annotation in order to eliminate this error.
                 */
            }
        });
        return notifier;
    }

    @NotNull
    public List<Test> asTestList(@NotNull Description description) {
        if (description.isTest()) {
            return Arrays.asList(asTest(description));
        } else {
            @NotNull List<Test> returnThis = new ArrayList<Test>();
            for (@NotNull Description child : description.getChildren()) {
                returnThis.add(asTest(child));
            }
            return returnThis;
        }
    }

}