package org.junit.runners;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InvalidTestClassError;
import org.junit.runners.model.TestClass;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParametersFactory;
import org.junit.runners.parameterized.ParametersRunnerFactory;
import org.junit.runners.parameterized.TestWithParameters;

/**
 * The custom runner <code>Parameterized</code> implements parameterized tests.
 * When running a parameterized test class, instances are created for the
 * cross-product of the test methods and the test data elements.
 * <p>
 * For example, to test the <code>+</code> operator, write:
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * public class AdditionTest {
 *     &#064;Parameters(name = &quot;{index}: {0} + {1} = {2}&quot;)
 *     public static Iterable&lt;Object[]&gt; data() {
 *         return Arrays.asList(new Object[][] { { 0, 0, 0 }, { 1, 1, 2 },
 *                 { 3, 2, 5 }, { 4, 3, 7 } });
 *     }
 *
 *     private int firstSummand;
 *
 *     private int secondSummand;
 *
 *     private int sum;
 *
 *     public AdditionTest(int firstSummand, int secondSummand, int sum) {
 *         this.firstSummand = firstSummand;
 *         this.secondSummand = secondSummand;
 *         this.sum = sum;
 *     }
 *
 *     &#064;Test
 *     public void test() {
 *         assertEquals(sum, firstSummand + secondSummand);
 *     }
 * }
 * </pre>
 * <p>
 * Each instance of <code>AdditionTest</code> will be constructed using the
 * three-argument constructor and the data values in the
 * <code>&#064;Parameters</code> method.
 * <p>
 * In order that you can easily identify the individual tests, you may provide a
 * name for the <code>&#064;Parameters</code> annotation. This name is allowed
 * to contain placeholders, which are replaced at runtime. The placeholders are
 * <dl>
 * <dt>{index}</dt>
 * <dd>the current parameter index</dd>
 * <dt>{0}</dt>
 * <dd>the first parameter value</dd>
 * <dt>{1}</dt>
 * <dd>the second parameter value</dd>
 * <dt>...</dt>
 * <dd>...</dd>
 * </dl>
 * <p>
 * In the example given above, the <code>Parameterized</code> runner creates
 * names like <code>[2: 3 + 2 = 5]</code>. If you don't use the name parameter,
 * then the current parameter index is used as name.
 * <p>
 * You can also write:
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * public class AdditionTest {
 *     &#064;Parameters(name = &quot;{index}: {0} + {1} = {2}&quot;)
 *     public static Iterable&lt;Object[]&gt; data() {
 *         return Arrays.asList(new Object[][] { { 0, 0, 0 }, { 1, 1, 2 },
 *                 { 3, 2, 5 }, { 4, 3, 7 } });
 *     }
 *
 *     &#064;Parameter(0)
 *     public int firstSummand;
 *
 *     &#064;Parameter(1)
 *     public int secondSummand;
 *
 *     &#064;Parameter(2)
 *     public int sum;
 *
 *     &#064;Test
 *     public void test() {
 *         assertEquals(sum, firstSummand + secondSummand);
 *     }
 * }
 * </pre>
 * <p>
 * Each instance of <code>AdditionTest</code> will be constructed with the default constructor
 * and fields annotated by <code>&#064;Parameter</code>  will be initialized
 * with the data values in the <code>&#064;Parameters</code> method.
 *
 * <p>
 * The parameters can be provided as an array, too:
 * 
 * <pre>
 * &#064;Parameters
 * public static Object[][] data() {
 * 	return new Object[][] { { 0, 0, 0 }, { 1, 1, 2 }, { 3, 2, 5 }, { 4, 3, 7 } } };
 * }
 * </pre>
 * 
 * <h3>Tests with single parameter</h3>
 * <p>
 * If your test needs a single parameter only, you don't have to wrap it with an
 * array. Instead you can provide an <code>Iterable</code> or an array of
 * objects.
 * <pre>
 * &#064;Parameters
 * public static Iterable&lt;? extends Object&gt; data() {
 * 	return Arrays.asList(&quot;first test&quot;, &quot;second test&quot;);
 * }
 * </pre>
 * <p>
 * or
 * <pre>
 * &#064;Parameters
 * public static Object[] data() {
 * 	return new Object[] { &quot;first test&quot;, &quot;second test&quot; };
 * }
 * </pre>
 *
 * <h3>Executing code before/after executing tests for specific parameters</h3>
 * <p>
 * If your test needs to perform some preparation or cleanup based on the
 * parameters, this can be done by adding public static methods annotated with
 * {@code @BeforeParam}/{@code @AfterParam}. Such methods should either have no
 * parameters or the same parameters as the test.
 * <pre>
 * &#064;BeforeParam
 * public static void beforeTestsForParameter(String onlyParameter) {
 *     System.out.println("Testing " + onlyParameter);
 * }
 * </pre>
 *
 * <h3>Create different runners</h3>
 * <p>
 * By default the {@code Parameterized} runner creates a slightly modified
 * {@link BlockJUnit4ClassRunner} for each set of parameters. You can build an
 * own {@code Parameterized} runner that creates another runner for each set of
 * parameters. Therefore you have to build a {@link ParametersRunnerFactory}
 * that creates a runner for each {@link TestWithParameters}. (
 * {@code TestWithParameters} are bundling the parameters and the test name.)
 * The factory must have a public zero-arg constructor.
 *
 * <pre>
 * public class YourRunnerFactory implements ParametersRunnerFactory {
 *     public Runner createRunnerForTestWithParameters(TestWithParameters test)
 *             throws InitializationError {
 *         return YourRunner(test);
 *     }
 * }
 * </pre>
 * <p>
 * Use the {@link UseParametersRunnerFactory} to tell the {@code Parameterized}
 * runner that it should use your factory.
 *
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * &#064;UseParametersRunnerFactory(YourRunnerFactory.class)
 * public class YourTest {
 *     ...
 * }
 * </pre>
 *
 * @since 4.0
 */
public class Parameterized extends Suite {
    /**
     * Annotation for a method which provides parameters to be injected into the
     * test class constructor by <code>Parameterized</code>. The method has to
     * be public and static.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Parameters {
        /**
         * Optional pattern to derive the test's name from the parameters. Use
         * numbers in braces to refer to the parameters or the additional data
         * as follows:
         * <pre>
         * {index} - the current parameter index
         * {0} - the first parameter value
         * {1} - the second parameter value
         * etc...
         * </pre>
         * <p>
         * Default value is "{index}" for compatibility with previous JUnit
         * versions.
         *
         * @return {@link MessageFormat} pattern string, except the index
         *         placeholder.
         * @see MessageFormat
         */
        @NotNull String name() default "{index}";
    }

    /**
     * Annotation for fields of the test class which will be initialized by the
     * method annotated by <code>Parameters</code>.
     * By using directly this annotation, the test class constructor isn't needed.
     * Index range must start at 0.
     * Default value is 0.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Parameter {
        /**
         * Method that returns the index of the parameter in the array
         * returned by the method annotated by <code>Parameters</code>.
         * Index range must start at 0.
         * Default value is 0.
         *
         * @return the index of the parameter.
         */
        int value() default 0;
    }

    /**
     * Add this annotation to your test class if you want to generate a special
     * runner. You have to specify a {@link ParametersRunnerFactory} class that
     * creates such runners. The factory must have a public zero-arg
     * constructor.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Target(ElementType.TYPE)
    public @interface UseParametersRunnerFactory {
        /**
         * @return a {@link ParametersRunnerFactory} class (must have a default
         *         constructor)
         */
        @NotNull Class<? extends ParametersRunnerFactory> value() default BlockJUnit4ClassRunnerWithParametersFactory.class;
    }

    /**
     * Annotation for {@code public static void} methods which should be executed before
     * evaluating tests with particular parameters.
     *
     * @see org.junit.BeforeClass
     * @see org.junit.Before
     * @since 4.13
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface BeforeParam {
    }

    /**
     * Annotation for {@code public static void} methods which should be executed after
     * evaluating tests with particular parameters.
     *
     * @see org.junit.AfterClass
     * @see org.junit.After
     * @since 4.13
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AfterParam {
    }

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public Parameterized(Class<?> klass) throws Throwable {
        this(klass, new RunnersFactory(klass));
    }

    private Parameterized(Class<?> klass, RunnersFactory runnersFactory) throws Exception {
        super(klass, runnersFactory.createRunners());
        validateBeforeParamAndAfterParamMethods(runnersFactory.parameterCount);
    }

    private void validateBeforeParamAndAfterParamMethods(Integer parameterCount)
            throws InvalidTestClassError {
        @NotNull List<Throwable> errors = new ArrayList<Throwable>();
        validatePublicStaticVoidMethods(Parameterized.BeforeParam.class, parameterCount, errors);
        validatePublicStaticVoidMethods(Parameterized.AfterParam.class, parameterCount, errors);
        if (!errors.isEmpty()) {
            throw new InvalidTestClassError(getTestClass().getJavaClass(), errors);
            /*
              This is a true positive. By looking at the implementation of getJavaClass(), we know that
              its return type is Nullable (and we've checked through TestClass.java where getJavaClass() is
              implemented in order to ensure that getJavaClass() might return null).
              And this contradicts the implementation of InvalidTestClassError()
              (src/main/java/org/junit/runners/model/InvalidTestClassError.java)
              which requires its first parameter to be NotNull.

              However, we cannot change that annotation to Nullable in order to eliminate this error,
              because if we change it to Nullable, createMessage() (InvalidTestClassError.java: line 22)
              will then call testClass.getName() (InvalidTestClassError.java: line 27),
              which will cause a NullPointerException.
             */
        }
    }

    private void validatePublicStaticVoidMethods(
            Class<? extends Annotation> annotation, @Nullable Integer parameterCount,
            @NotNull List<Throwable> errors) {
        @NotNull List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
        for (@NotNull FrameworkMethod fm : methods) {
            fm.validatePublicVoid(true, errors);
            if (parameterCount != null) {
                int methodParameterCount = fm.getMethod().getParameterTypes().length;
                /*
                  This is a false positive.
                  By looking at src/main/java/org/junit/runners/model/FrameworkMethod.java (where getMethod() is implemented),
                  we get to know that although the return type of fm.getMethod() is annotated Nullable,
                  actually it will never return null
                  (because the constructor of FrameworkMethod checks for nullity of its parameter
                  and will throw an exception if the parameter is actually null; otherwise, it initializes its field "method"
                  using the parameter).
                  Since fm.getMethod() won't return null, this error is a false positive.
                 */
                if (methodParameterCount != 0 && methodParameterCount != parameterCount) {
                    errors.add(new Exception("Method " + fm.getName()
                            + "() should have 0 or " + parameterCount + " parameter(s)"));
                }
            }
        }
    }

    private static class AssumptionViolationRunner extends Runner {
        @NotNull
        private final Description description;
        private final AssumptionViolatedException exception;

        AssumptionViolationRunner(TestClass testClass, String methodName,
                AssumptionViolatedException exception) {
            this.description = Description
                    .createTestDescription(testClass.getJavaClass(),
                            methodName + "() assumption violation");
            /*
              This is a true positive. By looking at the implementation of getJavaClass(), we know that
              its return type is Nullable (see src/main/java/org/junit/runners/model/TestClass.java).
              And this contradicts the implementation of createTestDescription()
              (src/main/java/org/junit/runner/Description.java: line 101)
              which requires its first parameter to be NotNull.

              However, we cannot change that annotation to Nullable in order to eliminate this error,
              because if we change it to Nullable, calling clazz.getName() (Description.java: line 102)
              will cause a NullPointerException.
             */
            this.exception = exception;
        }

        @NotNull
        @Override
        public Description getDescription() {
            return description;
        }

        @Override
        public void run(@NotNull RunNotifier notifier) {
            notifier.fireTestAssumptionFailed(new Failure(description, exception));
        }
    }

    private static class RunnersFactory {
        private static final ParametersRunnerFactory DEFAULT_FACTORY = new BlockJUnit4ClassRunnerWithParametersFactory();

        @NotNull
        private final TestClass testClass;
        @NotNull
        private final FrameworkMethod parametersMethod;
        private final List<Object> allParameters;
        private final int parameterCount;
        @Nullable
        private final Runner runnerOverride;

        private RunnersFactory(Class<?> klass) throws Throwable {
            testClass = new TestClass(klass);
            parametersMethod = getParametersMethod(testClass);
            List<Object> allParametersResult;
            @Nullable AssumptionViolationRunner assumptionViolationRunner = null;
            try {
                allParametersResult = allParameters(testClass, parametersMethod);
            } catch (AssumptionViolatedException e) {
                allParametersResult = Collections.emptyList();
                assumptionViolationRunner = new AssumptionViolationRunner(testClass,
                        parametersMethod.getName(), e);
            }
            allParameters = allParametersResult;
            runnerOverride = assumptionViolationRunner;
            parameterCount =
                    allParameters.isEmpty() ? 0 : normalizeParameters(allParameters.get(0)).length;
        }

        @NotNull
        private List<Runner> createRunners() throws Exception {
            if (runnerOverride != null) {
                return Collections.singletonList(runnerOverride);
            }
            Parameters parameters = parametersMethod.getAnnotation(Parameters.class);
            /*
              ? Calling getAnnotation() will always get an exception. And thus IntelliJ reports
              calling parameters.name() may throw a NullPointerException.
              This is not related to our evaluation. So ignore it.
             */
            return Collections.unmodifiableList(createRunnersForParameters(
                    allParameters, parameters.name(),
                    getParametersRunnerFactory()));
        }

        private ParametersRunnerFactory getParametersRunnerFactory()
                throws InstantiationException, IllegalAccessException {
            @Nullable UseParametersRunnerFactory annotation = testClass
                    .getAnnotation(UseParametersRunnerFactory.class);
            if (annotation == null) {
                return DEFAULT_FACTORY;
            } else {
                @NotNull Class<? extends ParametersRunnerFactory> factoryClass = annotation
                        .value();
                return factoryClass.newInstance();
            }
        }

        private TestWithParameters createTestWithNotNormalizedParameters(
                @NotNull String pattern, int index, Object parametersOrSingleParameter) {
            @NotNull Object[] parameters = normalizeParameters(parametersOrSingleParameter);
            return createTestWithParameters(testClass, pattern, index, parameters);
        }

        @NotNull
        private static Object[] normalizeParameters(Object parametersOrSingleParameter) {
            return (parametersOrSingleParameter instanceof Object[]) ? (Object[]) parametersOrSingleParameter
                    : new Object[] { parametersOrSingleParameter };
        }

        @NotNull
        @SuppressWarnings("unchecked")
        private static List<Object> allParameters(
                @NotNull TestClass testClass, FrameworkMethod parametersMethod) throws Throwable {
            Object parameters = parametersMethod.invokeExplosively(null);
            if (parameters instanceof List) {
                return (List<Object>) parameters;
            } else if (parameters instanceof Collection) {
                return new ArrayList<Object>((Collection<Object>) parameters);
            } else if (parameters instanceof Iterable) {
                @NotNull List<Object> result = new ArrayList<Object>();
                for (Object entry : ((Iterable<Object>) parameters)) {
                    result.add(entry);
                }
                return result;
            } else if (parameters instanceof Object[]) {
                return Arrays.asList((Object[]) parameters);
            } else {
                throw parametersMethodReturnedWrongType(testClass, parametersMethod);
            }
        }

        @NotNull
        private static FrameworkMethod getParametersMethod(TestClass testClass) throws Exception {
            @NotNull List<FrameworkMethod> methods = testClass
                    .getAnnotatedMethods(Parameters.class);
            for (@NotNull FrameworkMethod each : methods) {
                if (each.isStatic() && each.isPublic()) {
                    return each;
                }
            }

            throw new Exception("No public static parameters method on class "
                    + testClass.getName());
        }

        @NotNull
        private List<Runner> createRunnersForParameters(
                @NotNull Iterable<Object> allParameters, @NotNull String namePattern,
                @NotNull ParametersRunnerFactory runnerFactory) throws Exception {
            try {
                @NotNull List<TestWithParameters> tests = createTestsForParameters(
                        allParameters, namePattern);
                @NotNull List<Runner> runners = new ArrayList<Runner>();
                for (TestWithParameters test : tests) {
                    runners.add(runnerFactory
                            .createRunnerForTestWithParameters(test));
                }
                return runners;
            } catch (ClassCastException e) {
                throw parametersMethodReturnedWrongType(testClass, parametersMethod);
            }
        }

        @NotNull
        private List<TestWithParameters> createTestsForParameters(
                Iterable<Object> allParameters, @NotNull String namePattern)
                throws Exception {
            int i = 0;
            @NotNull List<TestWithParameters> children = new ArrayList<TestWithParameters>();
            for (Object parametersOfSingleTest : allParameters) {
                children.add(createTestWithNotNormalizedParameters(namePattern,
                        i++, parametersOfSingleTest));
            }
            return children;
        }

        private static Exception parametersMethodReturnedWrongType(
                TestClass testClass, FrameworkMethod parametersMethod) throws Exception {
            String className = testClass.getName();
            String methodName = parametersMethod.getName();
            @NotNull String message = MessageFormat.format(
                    "{0}.{1}() must return an Iterable of arrays.", className,
                    methodName);
            return new Exception(message);
        }

        private TestWithParameters createTestWithParameters(
                TestClass testClass, String pattern, int index,
                Object[] parameters) {
            String finalPattern = pattern.replaceAll("\\{index\\}",
                    Integer.toString(index));
            @NotNull String name = MessageFormat.format(finalPattern, parameters);
            return new TestWithParameters("[" + name + "]", testClass,
                    Arrays.asList(parameters));
        }
    }
}
