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

    @NotNull  // changed
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

    @NotNull  // changed
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