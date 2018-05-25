package org.junit.validator;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.runners.model.TestClass;

/**
 * Validates a single facet of a test class.
 * 
 * @since 4.12
 */
public interface TestClassValidator {
    /**
     * Validate a single facet of a test class.
     * 
     * @param testClass
     *            the {@link TestClass} that is validated.
     * @return the validation errors found by the validator.
     */
    @NotNull
    List<Exception> validateTestClass(TestClass testClass);
}
