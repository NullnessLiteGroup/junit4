package org.junit.internal.matchers;

import java.lang.reflect.Method;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.hamcrest.BaseMatcher;
import org.junit.internal.MethodSorter;

/**
 * Convenient base class for Matchers that require a non-null value of a specific type.
 * This simply implements the null check, checks the type and then casts.
 *
 * @author Joe Walnes
 * @deprecated Please use {@link org.hamcrest.TypeSafeMatcher}.
 */
@Deprecated
public abstract class TypeSafeMatcher<T> extends BaseMatcher<T> {

    private Class<?> expectedType;

    /**
     * Subclasses should implement this. The item will already have been checked for
     * the specific type and will never be null.
     */
    public abstract boolean matchesSafely(T item);

    protected TypeSafeMatcher() {
        expectedType = findExpectedType(getClass());
    }

    @SuppressWarnings("nullness")
    private static Class<?> findExpectedType(Class<?> fromClass) {
        // [dereference.of.nullable] FALSE_POSITIVE
        // This method is private so users can not call this method with fromClass being null,
        // which is the only case that c can be de-referenced to be null in the following case.
        // Besides, the Nullness Checker issues no warning for incompatible param fromClass,
        // which means the source code by developers doesn't call this method with null.
        for (Class<?> c = fromClass; c != Object.class; c = c.getSuperclass()) {
            for (@Nullable Method method : MethodSorter.getDeclaredMethods(c)) {
                if (isMatchesSafelyMethod(method)) {
                    return method.getParameterTypes()[0];
                }
            }
        }

        throw new Error("Cannot determine correct type for matchesSafely() method.");
    }

    private static boolean isMatchesSafelyMethod(Method method) {
        return "matchesSafely".equals(method.getName())
                && method.getParameterTypes().length == 1
                && !method.isSynthetic();
    }

    protected TypeSafeMatcher(Class<T> expectedType) {
        this.expectedType = expectedType;
    }

    /**
     * Method made final to prevent accidental override.
     * If you need to override this, there's no point on extending TypeSafeMatcher.
     * Instead, extend the {@link BaseMatcher}.
     */
    @SuppressWarnings({"unchecked"})
    public final boolean matches(@Nullable Object item) {
        return item != null
                && expectedType.isInstance(item)
                && matchesSafely((T) item);
    }
}
