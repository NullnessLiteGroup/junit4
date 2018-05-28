package org.junit.runners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Using <code>Suite</code> as a runner allows you to manually
 * build a suite containing tests from many classes. It is the JUnit 4 equivalent of the JUnit 3.8.x
 * static {@link junit.framework.Test} <code>suite()</code> method. To use it, annotate a class
 * with <code>@RunWith(Suite.class)</code> and <code>@SuiteClasses({TestClass1.class, ...})</code>.
 * When you run this class, it will run all the tests in all the suite classes.
 *
 * @since 4.0
 */
public class Suite extends ParentRunner<Runner> {
    /**
     * Returns an empty suite.
     */
    public static Runner emptySuite() {
        try {
            return new Suite((Class<?>) null, new Class<?>[0]);
            /*
              This is a true positive. By looking at the implementation of Suite() (line 124),
              we know that it requires its first parameter "klass" to be NotNull. If we change that annotation
              to Nullable, it will cause another (violation) error at line 101 because this constructor is calling
              another Suite constructor (line 135) which requires the respective parameter to be NotNull.
              If we continually change that constructor's parameter annotation, there will be a violation
              at line 125 when the method is calling builder.runners(klass, suiteClasses) because builder.runners()
              (src/main/java/org/junit/runners/model/RunnerBuilder.java: line 92) requires its first parameter
              to be NotNull. If we again change that annotation to Nullable, when builder.runners() calls addParent()
              (RunnerBuilder.java: line 94), there will be another violation due to the annotation. By continually
              change addParent()'s annotation, there finally occurs a potential NullPointerException
              (RunnerBuilder.java: line 75), because it calls parents.add() which indicates that parents variable
              should never be null.
              So, we cannot change any annotation to eliminate the original error.
              And it is a true positive because if we change the annotations continuously
              (due to the violations), we finally will meet a potential NullPointerException.
             */
        } catch (InitializationError e) {
            throw new RuntimeException("This shouldn't be possible");
        }
    }

    /**
     * The <code>SuiteClasses</code> annotation specifies the classes to be run when a class
     * annotated with <code>@RunWith(Suite.class)</code> is run.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface SuiteClasses {
        /**
         * @return the classes to be run
         */
        @NotNull Class<?>[] value();
    }

    @NotNull
    private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
        SuiteClasses annotation = klass.getAnnotation(SuiteClasses.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
        }
        return annotation.value();
    }

    @NotNull
    private final List<Runner> runners;

    /**
     * Called reflectively on classes annotated with <code>@RunWith(Suite.class)</code>
     *
     * @param klass the root class
     * @param builder builds runners for classes in the suite
     */
    public Suite(@NotNull Class<?> klass, @NotNull RunnerBuilder builder) throws InitializationError {
        this(builder, klass, getAnnotatedClasses(klass));
    }

    /**
     * Call this when there is no single root class (for example, multiple class names
     * passed on the command line to {@link org.junit.runner.JUnitCore}
     *
     * @param builder builds runners for classes in the suite
     * @param classes the classes in the suite
     */
    public Suite(RunnerBuilder builder, @NotNull Class<?>[] classes) throws InitializationError {
        this(null, builder.runners(null, classes));
        /*
          This is a true positive. By looking at the implementation builder.runners()
          (src/main/java/org/junit/runners/model/RunnerBuilder.java: line 92),
          we know that it requires the first parameter
          to be NotNull. If we change that annotation to Nullable, when builder.runners() calls addParent()
          (RunnerBuilder.java: line 94), there will be another violation due to the annotation. By continually
          change the respective annotation of addParent() to Nullable, there occurs a potential NullPointerException
          (RunnerBuilder.java: line 75), because addParent() calls parents.add() which indicates that parents variable
          should never be null.
          So, we cannot change any annotation to eliminate the original error.
          And it is a true positive because if we change the annotations continuously
          (due to the violations), we finally will meet a potential NullPointerException.
         */
    }

    /**
     * Call this when the default builder is good enough. Left in for compatibility with JUnit 4.4.
     *
     * @param klass the root of the suite
     * @param suiteClasses the classes in the suite
     */
    protected Suite(@NotNull Class<?> klass, @NotNull Class<?>[] suiteClasses) throws InitializationError {
        this(new AllDefaultPossibilitiesBuilder(), klass, suiteClasses);
    }

    /**
     * Called by this class and subclasses once the classes making up the suite have been determined
     *
     * @param builder builds runners for classes in the suite
     * @param klass the root of the suite
     * @param suiteClasses the classes in the suite
     */
    protected Suite(RunnerBuilder builder, @NotNull Class<?> klass, @NotNull Class<?>[] suiteClasses) throws InitializationError {
        this(klass, builder.runners(klass, suiteClasses));
    }

    /**
     * Called by this class and subclasses once the runners making up the suite have been determined
     *
     * @param klass root of the suite
     * @param runners for each class in the suite, a {@link Runner}
     */
    protected Suite(Class<?> klass, @NotNull List<Runner> runners) throws InitializationError {
        super(klass);
        this.runners = Collections.unmodifiableList(runners);
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    @Override
    protected Description describeChild(@NotNull Runner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(@NotNull Runner runner, final RunNotifier notifier) {
        runner.run(notifier);
    }
}
