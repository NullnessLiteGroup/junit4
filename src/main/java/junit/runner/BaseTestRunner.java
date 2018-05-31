package junit.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.Properties;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestSuite;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.Throwables;

/**
 * Base class for all test runners.
 * This class was born live on stage in Sardinia during XP2000.
 */
@SuppressWarnings("nullness")
public abstract class BaseTestRunner implements TestListener {
    // [initialization.fields.uninitialized] FALSE_POSITIVE
    //  fPreference is a safe field, which will not raise NPE
    // because it's private, and the only place to access it
    // getPreferences() ensures the returned value non-null
    public static final String SUITE_METHODNAME = "suite";

    private static Properties fPreferences;
    static int fgMaxMessageLength = 500;
    static boolean fgFilterStack = true;
    boolean fLoading = true;

    /*
    * Implementation of TestListener
    */
    public synchronized void startTest(Test test) {
        testStarted(test.toString());
    }

    protected static void setPreferences(Properties preferences) {
        fPreferences = preferences;
    }

    protected static Properties getPreferences() {
        if (fPreferences == null) {
            fPreferences = new Properties();
            fPreferences.put("loading", "true");
            fPreferences.put("filterstack", "true");
            readPreferences();
        }
        return fPreferences;
    }

    public static void savePreferences() throws IOException {
        FileOutputStream fos = new FileOutputStream(getPreferencesFile());
        try {
            getPreferences().store(fos, "");
        } finally {
            fos.close();
        }
    }

    public static void setPreference(String key, String value) {
        getPreferences().put(key, value);
    }

    public synchronized void endTest(Test test) {
        testEnded(test.toString());
    }

    // Nullable e from override super class TestListener
    public synchronized void addError(final Test test, final @Nullable Throwable e) {
        testFailed(TestRunListener.STATUS_ERROR, test, e);
    }

    public synchronized void addFailure(final Test test, final AssertionFailedError e) {
        testFailed(TestRunListener.STATUS_FAILURE, test, e);
    }

    // TestRunListener implementation

    public abstract void testStarted(String testName);

    public abstract void testEnded(String testName);

    // Nullable e from BaseTestRunner: addError
    public abstract void testFailed(int status, Test test, @Nullable Throwable e);

    /**
     * Returns the Test corresponding to the given suite. This is
     * a template method, subclasses override runFailed(), clearStatus().
     */
    // Nullable Test returned from getTest("")
    @SuppressWarnings("nullness")
    public @Nullable Test getTest(String suiteClassName) {
        if (suiteClassName.length() <= 0) {
            clearStatus();
            return null;
        }
        Class<?> testClass = null;
        try {
            testClass = loadSuiteClass(suiteClassName);
        } catch (ClassNotFoundException e) {
            String clazz = e.getMessage();
            if (clazz == null) {
                clazz = suiteClassName;
            }
            runFailed("Class not found \"" + clazz + "\"");
            return null;
        } catch (Exception e) {
            runFailed("Error: " + e.toString());
            return null;
        }
        Method suiteMethod = null;
        try {
            suiteMethod = testClass.getMethod(SUITE_METHODNAME);
        } catch (Exception e) {
            // try to extract a test suite automatically
            clearStatus();
            return new TestSuite(testClass);
        }
        if (!Modifier.isStatic(suiteMethod.getModifiers())) {
            runFailed("Suite() method must be static");
            return null;
        }
        Test test = null;
        try {
            test = (Test) suiteMethod.invoke(null); // static method
            if (test == null) {
                return test;
            }
        } catch (InvocationTargetException e) {
            // [throwing.nullable] TRUE_POSITIVE
            // e.getTargetException() is nullable from the
            // documentation of getCause(), a substitute method
            // since the release of Java 1.4;
            // Although e.getCause() in InvocationTargetException
            // may be intended to be non-null in Java reflection,
            // we decided it JUnit4 needs to be safer.
            // @See Java API that documents getCause can be null
            //      (https://docs.oracle.com/javase/8/docs/api/
            //      java/lang/reflect/InvocationTargetException.html)
            // @See StackOverFlow discussion about when InvocationTargetException
            //      has a null cause (https://stackoverflow.com/questions/
            //      17684484/when-is-invocationtargetexception-getcause-null)
            // @See The blog in Oracle forum discussing the four possibilities of
            //      getCause() (https://blogs.oracle.com/chengfang/
            //      whats-inside-invocationtargetexception-not-just-exception)
            runFailed("Failed to invoke suite():" + e.getTargetException().toString());
            return null;
        } catch (IllegalAccessException e) {
            runFailed("Failed to invoke suite():" + e.toString());
            return null;
        }

        clearStatus();
        return test;
    }

    /**
     * Returns the formatted string of the elapsed time.
     */
    public String elapsedTimeAsString(long runTime) {
        return NumberFormat.getInstance().format((double) runTime / 1000);
    }

    /**
     * Processes the command line arguments and
     * returns the name of the suite class to run or null
     */
    // Nullable String returned by documentation above
    protected @Nullable String processArguments(String[] args) {
        String suiteName = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-noloading")) {
                setLoading(false);
            } else if (args[i].equals("-nofilterstack")) {
                fgFilterStack = false;
            } else if (args[i].equals("-c")) {
                if (args.length > i + 1) {
                    suiteName = extractClassName(args[i + 1]);
                } else {
                    System.out.println("Missing Test class name");
                }
                i++;
            } else {
                suiteName = args[i];
            }
        }
        return suiteName;
    }

    /**
     * Sets the loading behaviour of the test runner
     */
    public void setLoading(boolean enable) {
        fLoading = enable;
    }

    /**
     * Extract the class name from a String in VA/Java style
     */
    public String extractClassName(String className) {
        if (className.startsWith("Default package for")) {
            return className.substring(className.lastIndexOf(".") + 1);
        }
        return className;
    }

    /**
     * Truncates a String to the maximum length.
     */
    public static String truncate(String s) {
        if (fgMaxMessageLength != -1 && s.length() > fgMaxMessageLength) {
            s = s.substring(0, fgMaxMessageLength) + "...";
        }
        return s;
    }

    /**
     * Override to define how to handle a failed loading of
     * a test suite.
     */
    protected abstract void runFailed(String message);

    /**
     * Returns the loaded Class for a suite name.
     */
    protected Class<?> loadSuiteClass(String suiteClassName) throws ClassNotFoundException {
        return Class.forName(suiteClassName);
    }

    /**
     * Clears the status message.
     */
    protected void clearStatus() { // Belongs in the GUI TestRunner class
    }

    @SuppressWarnings("nullness")
    protected boolean useReloadingTestSuiteLoader() {
        // [dereference.of.nullable] FALSE_POSITIVE
        //  de-referencing getPreference("loading") cannot raise NPE
        // getPreferences, called by getPreference, always returns
        // Properties that has key "loading"
        return getPreference("loading").equals("true") && fLoading;
    }

    private static File getPreferencesFile() {
        String home = System.getProperty("user.home");
        return new File(home, "junit.properties");
    }

    private static void readPreferences() {
        InputStream is = null;
        try {
            is = new FileInputStream(getPreferencesFile());
            setPreferences(new Properties(getPreferences()));
            getPreferences().load(is);
        } catch (IOException ignored) {
        } catch (SecurityException ignored) {
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e1) {
            }
        }
    }

    // Nullable String returned if key is not found in Property
    public static @Nullable String getPreference(String key) {
        return getPreferences().getProperty(key);
    }

    public static int getPreference(String key, int dflt) {
        String value = getPreference(key);
        int intValue = dflt;
        if (value == null) {
            return intValue;
        }
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException ne) {
        }
        return intValue;
    }

    /**
     * Returns a filtered stack trace
     */
    public static String getFilteredTrace(Throwable e) {
        return BaseTestRunner.getFilteredTrace(Throwables.getStacktrace(e));
    }

    /**
     * Filters stack frames from internal JUnit classes
     */
    public static String getFilteredTrace(String stack) {
        if (showStackRaw()) {
            return stack;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StringReader sr = new StringReader(stack);
        BufferedReader br = new BufferedReader(sr);

        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (!filterLine(line)) {
                    pw.println(line);
                }
            }
        } catch (Exception IOException) {
            return stack; // return the stack unfiltered
        }
        return sw.toString();
    }

    @SuppressWarnings("nullness")
    protected static boolean showStackRaw() {
        // [dereference.of.nullable] FALSE_POSITIVE
        //  de-referencing getPreference("filterstack") cannot raise NPE
        // getPreferences, called by getPreference, always returns
        // Properties that has key "filterstack"
        return !getPreference("filterstack").equals("true") || fgFilterStack == false;
    }

    static boolean filterLine(String line) {
        String[] patterns = new String[]{
                "junit.framework.TestCase",
                "junit.framework.TestResult",
                "junit.framework.TestSuite",
                "junit.framework.Assert.", // don't filter AssertionFailure
                "junit.swingui.TestRunner",
                "junit.awtui.TestRunner",
                "junit.textui.TestRunner",
                "java.lang.reflect.Method.invoke("
        };
        for (int i = 0; i < patterns.length; i++) {
            if (line.indexOf(patterns[i]) > 0) {
                return true;
            }
        }
        return false;
    }

    static {
        fgMaxMessageLength = getPreference("maxmessage", fgMaxMessageLength);
    }

}
