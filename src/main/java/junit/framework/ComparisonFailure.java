package junit.framework;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Thrown when an assert equals for Strings failed.
 *
 * Inspired by a patch from Alex Chaffee mailto:alex@purpletech.com
 */
public class ComparisonFailure extends AssertionFailedError {
    private static final int MAX_CONTEXT_LENGTH = 20;
    private static final long serialVersionUID = 1L;

    private @Nullable String fExpected;
    private @Nullable String fActual;

    /**
     * Constructs a comparison failure.
     *
     * @param message the identifying message or null
     * @param expected the expected string value
     * @param actual the actual string value
     */
    public ComparisonFailure(@Nullable String message, @Nullable String expected, @Nullable String actual) {
        super(message);
        fExpected = expected;
        fActual = actual;
    }

    /**
     * Returns "..." in place of common prefix and "..." in
     * place of common suffix between expected and actual.
     *
     * @see Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return new ComparisonCompactor(MAX_CONTEXT_LENGTH, fExpected, fActual).compact(super.getMessage());
    }

    /**
     * Gets the actual string value
     *
     * @return the actual string value
     */
    public @Nullable String getActual() {
        return fActual;
    }

    /**
     * Gets the expected string value
     *
     * @return the expected string value
     */
    public @Nullable String getExpected() {
        return fExpected;
    }
}