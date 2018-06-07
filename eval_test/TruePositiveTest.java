package practice;

import org.junit.Test;
import org.junit.experimental.max.MaxCore;
import org.junit.experimental.results.PrintableResult;
import org.junit.experimental.results.ResultMatchers;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.TestClass;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class TruePositiveTest {
    @Test(expected=NullPointerException.class)
    public void testDescription1() {
        Description.createTestDescription((Class<Object>) null, "", (Annotation[]) null);
    }

    @Test(expected=NullPointerException.class)
    public void testDescription2() {
        Description.createTestDescription((Class<Object>) null, "");
    }

    static Failure failure = (new Failure(Description.createTestDescription(Object.class, ""), null));

    @Test(expected=NullPointerException.class)
    public void testFailure1() {
        failure.toString();
    }

    @Test(expected=NullPointerException.class)
    public void testFailure2() {
        failure.getMessage();
    }

    static TestClass testClass = (new TestClass(null));

    @Test(expected=NullPointerException.class)
    public void testTestClass1() {
        testClass.getOnlyConstructor();
    }

    @Test(expected=NullPointerException.class)
    public void testTestClass2() {
        testClass.isPublic();
    }

    @Test(expected=NullPointerException.class)
    public void testTestClass3() {
        testClass.isANonStaticInnerClass();
    }

    @Test(expected=NullPointerException.class)
    public void testMaxCore() {
        MaxCore.storedLocally(new File("/* some path */")).sortRequest(Request.runner(null));
    }

    @Test(expected=NullPointerException.class)
    public void testJUnitCore() {
        (new JUnitCore()).run(Request.runner(null));
    }

    @Test(expected=NullPointerException.class)
    public void testResultMatchers() {
        ResultMatchers.hasSingleFailureContaining(null).matches(null);
    }

    @Test(expected=NullPointerException.class)
    public void testThrowables1() {
        (new Failure(null, null)).getTrace();
    }

    @Test(expected=NullPointerException.class)
    public void testThrowables2() {
        List<Failure> list = new ArrayList<>();
        list.add(new Failure(Description.createTestDescription(Object.class, ""), null));
        (new PrintableResult(list)).toString();
    }
}