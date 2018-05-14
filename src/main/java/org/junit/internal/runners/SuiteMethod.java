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

    // Nullable Test returned from suiteMethod.invoke(null)
    //      it is possible for a customized suite method return nothing
    public static @Nullable Test testFromSuiteMethod(Class<?> klass) throws Throwable {
        Method suiteMethod = null;
        Test suite = null;
        try {
            suiteMethod = klass.getMethod("suite");
            if (!Modifier.isStatic(suiteMethod.getModifiers())) {
                throw new Exception(klass.getName() + ".suite() must be static");
            }
            suite = (Test) suiteMethod.invoke(null); // static method
        } catch (InvocationTargetException e) {
            // [throwing.nullable] FALSE_POSITIVE
            //  de-referencing e is safe here
            // nowhere else in the project called InvocationTargetException(),
            // and the only public constructor of InvocationTargetException
            // ensures target non-null
            throw e.getCause();
        }
        return suite;
    }
}
