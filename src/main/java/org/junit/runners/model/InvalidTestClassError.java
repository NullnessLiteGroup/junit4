package org.junit.runners.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * Thrown by {@link org.junit.runner.Runner}s in case the class under test is not valid.
 * <p>
 * Its message conveniently lists all of the validation errors.
 *
 * @since 4.13
 */
public class InvalidTestClassError extends InitializationError {
    private static final long serialVersionUID = 1L;

    private final String message;

    // Nullable offendingTestClass from validate(ParentRunner<T> this)
    public InvalidTestClassError(@Nullable Class<?> offendingTestClass, List<Throwable> validationErrors) {
        super(validationErrors);
        this.message = createMessage(offendingTestClass, validationErrors);
    }

    // Nullable testClass from the constructor
    private static String createMessage(@Nullable Class<?> testClass, List<Throwable> validationErrors) {
        StringBuilder sb = new StringBuilder();
        // [dereference.of.nullable] TRUE_POSITIVE
        // dereference of possibly-null reference testClass
        // testClass could be passed in as null which may raise NPE
        sb.append(String.format("Invalid test class '%s':", testClass.getName()));
        int i = 1;
        for (Throwable error : validationErrors) {
            sb.append("\n  " + (i++) + ". " + error.getMessage());
        }
        return sb.toString();
    }

    /**
     * @return a message with a list of all of the validation errors
     */
    @Override
    public String getMessage() {
        return message;
    }
}
