package junit.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.MethodSorter;
import org.junit.internal.Throwables;

/**
 * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
 * It runs a collection of test cases. Here is an example using
 * the dynamic test definition.
 * <pre>
 * TestSuite suite= new TestSuite();
 * suite.addTest(new MathTest("testAdd"));
 * suite.addTest(new MathTest("testDivideByZero"));
 * </pre>
 * <p>
 * Alternatively, a TestSuite can extract the tests to be run automatically.
 * To do so you pass the class of your TestCase class to the
 * TestSuite constructor.
 * <pre>
 * TestSuite suite= new TestSuite(MathTest.class);
 * </pre>
 * <p>
 * This constructor creates a suite with all the methods
 * starting with "test" that take no arguments.
 * <p>
 * A final option is to do the same for a large array of test classes.
 * <pre>
 * Class[] testClasses = { MathTest.class, AnotherTest.class };
 * TestSuite suite= new TestSuite(testClasses);
 * </pre>
 *
 * @see Test
 */
public class TestSuite implements Test {

    /**
     * ...as the moon sets over the early morning Merlin, Oregon
     * mountains, our intrepid adventurers type...
     */
    static public Test createTest(Class<?> theClass, String name) {
        Constructor<?> constructor;
        try {
            constructor = getTestConstructor(theClass);
        } catch (NoSuchMethodException e) {
            return warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()");
        }
        Object test;
        try {
            if (constructor.getParameterTypes().length == 0) {
                test = constructor.newInstance(new Object[0]);
                if (test instanceof TestCase) {
                    ((TestCase) test).setName(name);
                }
            } else {
                test = constructor.newInstance(new Object[]{name});
            }
        } catch (InstantiationException e) {
            return (warning("Cannot instantiate test case: " + name + " (" + Throwables.getStacktrace(e) + ")"));
        } catch (InvocationTargetException e) {
            return (warning("Exception in constructor: " + name + " (" + Throwables.getStacktrace(e.getTargetException()) + ")"));
        } catch (IllegalAccessException e) {
            return (warning("Cannot access test case: " + name + " (" + Throwables.getStacktrace(e) + ")"));
        }
        return (Test) test;
    }

    /**
     * Gets a constructor which takes a single String as
     * its argument or a no arg constructor.
     */
    public static Constructor<?> getTestConstructor(Class<?> theClass) throws NoSuchMethodException {
        try {
            return theClass.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            // fall through
        }
        return theClass.getConstructor();
    }

    /**
     * Returns a test which will fail and log a warning message.
     */
    public static Test warning(final String message) {
        return new TestCase("warning") {
            @Override
            protected void runTest() {
                fail(message);
            }
        };
    }

    private @Nullable String fName;

    private Vector<Test> fTests = new Vector<Test>(10); // Cannot convert this to List because it is used directly by some test runners

    /**
     * Constructs an empty TestSuite.
     */
    public TestSuite() {
    }

    /**
     * Constructs a TestSuite from the given class. Adds all the methods
     * starting with "test" as test cases to the suite.
     * Parts of this method were written at 2337 meters in the Hueffihuette,
     * Kanton Uri
     */
    public TestSuite(final Class<?> theClass) {
        addTestsFromTestCase(theClass);
    }

    // helper method to for the constructor of TestSuite
    private void addTestsFromTestCase(@UnderInitialization TestSuite this, final Class<?> theClass) {
        fName = theClass.getName();
        try {
            getTestConstructor(theClass); // Avoid generating multiple error messages
        } catch (NoSuchMethodException e) {
            addTest(warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()"));
            return;
        }

        if (!Modifier.isPublic(theClass.getModifiers())) {
            addTest(warning("Class " + theClass.getName() + " is not public"));
            return;
        }

        Class<?> superClass = theClass;
        List<String> names = new ArrayList<String>();
        // [argument.type.incompatible] FALSE_POSITIVE
        // superClass cannot be nullable Class<? extends Object> here
        // because superClass = theClass, and theClass cannot be null
        // here, otherwise NPE will be raised at the beginning of the method
        while (Test.class.isAssignableFrom(superClass)) {
            for (Method each : MethodSorter.getDeclaredMethods(superClass)) {
                addTestMethod(each, names, theClass);
            }
            superClass = superClass.getSuperclass();
        }
        // [dereference.of.nullable] FALSE_POSITIVE
        // fTests cannot be null here, it is already assigned some value
        // up in the declaration; As a private field, it is never reassigned
        // to be null in this class
        if (fTests.size() == 0) {
            addTest(warning("No tests found in " + theClass.getName()));
        }
    }

    /**
     * Constructs a TestSuite from the given class with the given name.
     *
     * @see TestSuite#TestSuite(Class)
     */
    public TestSuite(Class<? extends TestCase> theClass, String name) {
        this(theClass);
        setName(name);
    }

    /**
     * Constructs an empty TestSuite.
     */
    // Nullable name from JUnit38ClassRunner.filter(Filter filter)
    public TestSuite(@Nullable String name) {
        setName(name);
    }

    /**
     * Constructs a TestSuite from the given array of classes.
     *
     * @param classes {@link TestCase}
     */
    public TestSuite(Class<?>... classes) {
        for (Class<?> each : classes) {
            addTest(testCaseForClass(each));
        }
    }

    // helper method to for the constructor of TestSuite
    private Test testCaseForClass(@UnknownInitialization TestSuite this, Class<?> each) {
        if (TestCase.class.isAssignableFrom(each)) {
            return new TestSuite(each.asSubclass(TestCase.class));
        } else {
            return warning(each.getCanonicalName() + " does not extend TestCase");
        }
    }

    /**
     * Constructs a TestSuite from the given array of classes with the given name.
     *
     * @see TestSuite#TestSuite(Class[])
     */
    public TestSuite(Class<? extends TestCase>[] classes, String name) {
        this(classes);
        setName(name);
    }

    /**
     * Adds a test to the suite.
     */
    // helper method to for the constructor of TestSuite
    public void addTest(@UnknownInitialization TestSuite this, Test test) {
        // [dereference.of.nullable] FALSE_POSITIVE
        //  fTests cannot be null here, it is already assigned some value
        // up in the declaration; As a private field, it is never reassigned
        // to be null in this class
        fTests.add(test);
    }

    /**
     * Adds the tests from the given class to the suite.
     */
    public void addTestSuite(Class<? extends TestCase> testClass) {
        addTest(new TestSuite(testClass));
    }

    /**
     * Counts the number of test cases that will be run by this test.
     */
    public int countTestCases() {
        int count = 0;
        for (Test each : fTests) {
            count += each.countTestCases();
        }
        return count;
    }

    /**
     * Returns the name of the suite. Not all
     * test suites have a name and this method
     * can return null.
     */
    // Nullable String returned due to documentation above
    public @Nullable String getName() {
        return fName;
    }

    /**
     * Runs the tests and collects their result in a TestResult.
     */
    public void run(TestResult result) {
        for (Test each : fTests) {
            if (result.shouldStop()) {
                break;
            }
            runTest(each, result);
        }
    }

    public void runTest(Test test, TestResult result) {
        test.run(result);
    }

    /**
     * Sets the name of the suite.
     *
     * @param name the name to set
     */
    // helper method for the constructor of TestSuite
    // Nullable name from TestSuite(String name)
    public void setName(@UnknownInitialization TestSuite this, @Nullable String name) {
        fName = name;
    }

    /**
     * Returns the test at the given index.
     */
    public Test testAt(int index) {
        return fTests.get(index);
    }

    /**
     * Returns the number of tests in this suite.
     */
    public int testCount() {
        return fTests.size();
    }

    /**
     * Returns the tests as an enumeration.
     */
    public Enumeration<Test> tests() {
        return fTests.elements();
    }

    /**
     */
    @Override
    public String toString() {
        if (getName() != null) {
            // [return.type.incompatible] FALSE_POSITIVE
            // getName() will not be null in this case
            // because we ensured getName() is non-null above
            // However, we cannot reduce this false-positive by
            // annotate deterministic because users can
            // change fName by setName(String name)
            return getName();
        }
        return super.toString();
    }

    // call from addTestsFromTestCase: helper method to for the constructor of TestSuite
    private void addTestMethod(@UnderInitialization TestSuite this, Method m, List<String> names, Class<?> theClass) {
        String name = m.getName();
        if (names.contains(name)) {
            return;
        }
        if (!isPublicTestMethod(m)) {
            if (isTestMethod(m)) {
                addTest(warning("Test method isn't public: " + m.getName() + "(" + theClass.getCanonicalName() + ")"));
            }
            return;
        }
        names.add(name);
        addTest(createTest(theClass, name));
    }

    // call from addTestMethod: helper method to for the constructor of TestSuite
    private boolean isPublicTestMethod(@UnderInitialization TestSuite this,Method m) {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }

    // call from addTestMethod: helper method to for the constructor of TestSuite
    private boolean isTestMethod(@UnderInitialization TestSuite this, Method m) {
        return m.getParameterTypes().length == 0 &&
                m.getName().startsWith("test") &&
                m.getReturnType().equals(Void.TYPE);
    }
}
