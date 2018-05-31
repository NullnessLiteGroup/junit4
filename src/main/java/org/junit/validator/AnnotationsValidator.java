package org.junit.validator;

import static java.util.Collections.singletonList;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.runners.model.Annotatable;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

/**
 * An {@code AnnotationsValidator} validates all annotations of a test class,
 * including its annotated fields and methods.
 * 
 * @since 4.12
 */
public final class AnnotationsValidator implements TestClassValidator {
    private static final List<AnnotatableValidator<?>> VALIDATORS = Arrays.<AnnotatableValidator<?>>asList(
            new ClassValidator(), new MethodValidator(), new FieldValidator());

    /**
     * Validate all annotations of the specified test class that are be
     * annotated with {@link ValidateWith}.
     * 
     * @param testClass
     *            the {@link TestClass} that is validated.
     * @return the errors found by the validator.
     */
    @NotNull
    public List<Exception> validateTestClass(TestClass testClass) {
        @NotNull List<Exception> validationErrors= new ArrayList<Exception>();
        for (@NotNull AnnotatableValidator<?> validator : VALIDATORS) {
            @NotNull List<Exception> additionalErrors= validator
                    .validateTestClass(testClass);
            validationErrors.addAll(additionalErrors);
        }
        return validationErrors;
    }

    private static abstract class AnnotatableValidator<T extends Annotatable> {
        private static final AnnotationValidatorFactory ANNOTATION_VALIDATOR_FACTORY = new AnnotationValidatorFactory();

        @NotNull
        abstract Iterable<T> getAnnotatablesForTestClass(TestClass testClass);

        abstract List<Exception> validateAnnotatable(
                AnnotationValidator validator, T annotatable);

        @NotNull
        public List<Exception> validateTestClass(TestClass testClass) {
            @NotNull List<Exception> validationErrors= new ArrayList<Exception>();
            for (@NotNull T annotatable : getAnnotatablesForTestClass(testClass)) {
                @NotNull List<Exception> additionalErrors= validateAnnotatable(annotatable);
                validationErrors.addAll(additionalErrors);
            }
            return validationErrors;
        }

        @NotNull
        private List<Exception> validateAnnotatable(T annotatable) {
            @NotNull List<Exception> validationErrors= new ArrayList<Exception>();
            for (@NotNull Annotation annotation : annotatable.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation
                        .annotationType();
                ValidateWith validateWith = annotationType
                        .getAnnotation(ValidateWith.class);
                if (validateWith != null) {
                    AnnotationValidator annotationValidator = ANNOTATION_VALIDATOR_FACTORY
                            .createAnnotationValidator(validateWith);
                    List<Exception> errors= validateAnnotatable(
                            annotationValidator, annotatable);
                    validationErrors.addAll(errors);
                }
            }
            return validationErrors;
        }
    }

    private static class ClassValidator extends AnnotatableValidator<TestClass> {
        @NotNull
        @Override
        Iterable<TestClass> getAnnotatablesForTestClass(TestClass testClass) {
            return singletonList(testClass);
        }

        @Override
        List<Exception> validateAnnotatable(
                @NotNull AnnotationValidator validator, TestClass testClass) {
            return validator.validateAnnotatedClass(testClass);
        }
    }

    private static class MethodValidator extends
            AnnotatableValidator<FrameworkMethod> {
        @NotNull
        @Override
        Iterable<FrameworkMethod> getAnnotatablesForTestClass(
                @NotNull TestClass testClass) {
            return testClass.getAnnotatedMethods();
        }

        @Override
        List<Exception> validateAnnotatable(
                @NotNull AnnotationValidator validator, FrameworkMethod method) {
            return validator.validateAnnotatedMethod(method);
        }
    }

    private static class FieldValidator extends
            AnnotatableValidator<FrameworkField> {
        @NotNull
        @Override
        Iterable<FrameworkField> getAnnotatablesForTestClass(@NotNull TestClass testClass) {
            return testClass.getAnnotatedFields();
        }

        @Override
        List<Exception> validateAnnotatable(
                @NotNull AnnotationValidator validator, FrameworkField field) {
            return validator.validateAnnotatedField(field);
        }
    }
}
