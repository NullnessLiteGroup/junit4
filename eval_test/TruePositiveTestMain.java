package nltest;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TruePositiveTestMain {
    public static void main(String[] args) {
        Class[] classes = new Class[]{TruePositiveTest.class};
        final Result result = JUnitCore.runClasses(classes);
        for (final Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println(result.getFailures().isEmpty() ?
                "Test Passed: All NPEs raised" : "Test Failed: Some NPEs are not raised");
    }
}
