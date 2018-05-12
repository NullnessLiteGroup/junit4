package org.junit.internal.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;

public class ThrowableMessageMatcher<T extends Throwable> extends
        TypeSafeMatcher<T> {

    private final Matcher<String> matcher;

    public ThrowableMessageMatcher(Matcher<String> matcher) {
        this.matcher = matcher;
    }

    public void describeTo(@NotNull Description description) {
        description.appendText("exception with message ");
        description.appendDescriptionOf(matcher);
    }

    @Override
    protected boolean matchesSafely(@NotNull T item) {
        return matcher.matches(item.getMessage());
    }

    @Override
    protected void describeMismatchSafely(@NotNull T item, Description description) {
        description.appendText("message ");
        matcher.describeMismatch(item.getMessage(), description);
    }

    @Factory
    public static <T extends Throwable> Matcher<T> hasMessage(final Matcher<String> matcher) {
        return new ThrowableMessageMatcher<T>(matcher);
    }
}