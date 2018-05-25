package org.junit.rules;

import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.matchers.JUnitMatchers.isThrowable;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

/**
 * Builds special matcher used by {@link ExpectedException}.
 */
class ExpectedExceptionMatcherBuilder {

    private final List<Matcher<?>> matchers = new ArrayList<Matcher<?>>();

    void add(Matcher<?> matcher) {
        matchers.add(matcher);
    }

    boolean expectsThrowable() {
        return !matchers.isEmpty();
    }

    @NotNull
    Matcher<Throwable> build() {
        return isThrowable(allOfTheMatchers());
    }

    @NotNull
    private Matcher<Throwable> allOfTheMatchers() {
        if (matchers.size() == 1) {
            return cast(matchers.get(0));
        }
        return allOf(castedMatchers());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Matcher<? super Throwable>> castedMatchers() {
        return new ArrayList<Matcher<? super Throwable>>((List) matchers);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private Matcher<Throwable> cast(@NotNull Matcher<?> singleMatcher) {
        return (Matcher<Throwable>) singleMatcher;
    }
}
