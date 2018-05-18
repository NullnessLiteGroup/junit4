package org.junit.internal.runners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Test;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Runner for use with JUnit 3.8.x-style AllTests classes
 * (those that only implement a static <code>suite()</code>
 * method). For example:
 * <pre>
 * &#064;RunWith(AllTests.class)
 * public class ProductTests {
 *    public static junit.framework.Test suite() {
 *       ...
 *    }
 * }
 * </pre>
 */
public class SuiteMethod extends JUnit38ClassRunner {
    public SuiteMethod(Class<?> klass) throws Throwable {
        super(testFromSuiteMethod(klass));
    }

    public static Test testFromSuiteMethod(Class<?> klass) throws Throwable {
        Method suiteMethod = null;
        Test suite = null;
        try {
            suiteMethod = klass.getMethod("suite");
            if (!Modifier.isStatic(suiteMethod.getModifiers())) {
                throw new Exception(klass.getName() + ".suite() must be static");
            }
            suite = (Test) suiteMethod.invoke(null); // static method
        } catch (InvocationTargetException e) {
            // [throwing.nullable] TRUE_POSITIVE
            // the public constructor InvocationTargetException(target)
            // doesn't prevent null target, which is returned by e.getCause()
            throw e.getCause();
        }
        // [return.type.incompatible] FALSE_POSITIVE
        // suite cannot be null here
        // SuiteMethod is an internal class not exposed to users in JUnit4 API
        // its two callers: 1) AllTests is documented not used programmatically
        // although it is exposed to users
        // 2) SuiteMethodBuilder.runnerForClass ensures the klass here has a public
        // static suite method with no parameter.
        // And we checked all tests files and noticed that all public static suite()
        // methods return non-null Test
        return suite;
    }
}
