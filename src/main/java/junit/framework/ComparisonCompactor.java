package junit.framework;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ComparisonCompactor {

    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";

    private int fContextLength;
    private @Nullable String fExpected;
    private @Nullable String fActual;
    private int fPrefix;
    private int fSuffix;

    public ComparisonCompactor(int contextLength, @Nullable String expected, @Nullable String actual) {
        fContextLength = contextLength;
        fExpected = expected;
        fActual = actual;
    }

    @SuppressWarnings({"deprecation","nullness"})
    public String compact(@Nullable String message) {
        if (fExpected == null || fActual == null || areStringsEqual()) {
            return Assert.format(message, fExpected, fActual);
        }

        findCommonPrefix();
        findCommonSuffix();
        // [argument.type.incompatible] FALSE_POSITIVE
        //  fExpected is refined non-null here,
        // otherwise captured by the code above
        String expected = compactString(fExpected);
        // [argument.type.incompatible] FALSE_POSITIVE
        //  fActual is refined non-null here,
        // otherwise captured by the code above
        String actual = compactString(fActual);
        return Assert.format(message, expected, actual);
    }

    private String compactString(String source) {
        String result = DELTA_START + source.substring(fPrefix, source.length() - fSuffix + 1) + DELTA_END;
        if (fPrefix > 0) {
            result = computeCommonPrefix() + result;
        }
        if (fSuffix > 0) {
            result = result + computeCommonSuffix();
        }
        return result;
    }

    @SuppressWarnings("nullness")
    private void findCommonPrefix() {
        fPrefix = 0;
        // [dereference.of.nullable] FALSE_POSITIVE
        //  fExpected is ensured non-null here
        // since the caller compact() captured the null value
        //
        // [dereference.of.nullable] FALSE_POSITIVE
        //  fActual is ensured non-null here
        // since the caller compact() captured the null value
        int end = Math.min(fExpected.length(), fActual.length());
        for (; fPrefix < end; fPrefix++) {
            if (fExpected.charAt(fPrefix) != fActual.charAt(fPrefix)) {
                break;
            }
        }
    }

    @SuppressWarnings("nullness")
    private void findCommonSuffix() {
        // [dereference.of.nullable] FALSE_POSITIVE
        //  fExpected is ensured non-null here
        // since the caller compact() captured the null value
        int expectedSuffix = fExpected.length() - 1;
        // [dereference.of.nullable] FALSE_POSITIVE
        //  fActual is ensured non-null here
        // since the caller compact() captured the null value
        int actualSuffix = fActual.length() - 1;
        for (; actualSuffix >= fPrefix && expectedSuffix >= fPrefix; actualSuffix--, expectedSuffix--) {
            if (fExpected.charAt(expectedSuffix) != fActual.charAt(actualSuffix)) {
                break;
            }
        }
        fSuffix = fExpected.length() - expectedSuffix;
    }

    @SuppressWarnings("nullness")
    private String computeCommonPrefix() {
        // [dereference.of.nullable] FALSE_POSITIVE
        //  fExpected is ensured non-null here
        // since the caller compactString() is only called in compact()
        // when fExpected is non-null
        return (fPrefix > fContextLength ? ELLIPSIS : "") + fExpected.substring(Math.max(0, fPrefix - fContextLength), fPrefix);
    }

    @SuppressWarnings("nullness")
    private String computeCommonSuffix() {
        // [dereference.of.nullable] FALSE_POSITIVE
        //  fExpected is ensured non-null here
        // since the caller compactString() is only called in compact()
        // when fExpected is non-null
        int end = Math.min(fExpected.length() - fSuffix + 1 + fContextLength, fExpected.length());
        return fExpected.substring(fExpected.length() - fSuffix + 1, end) + (fExpected.length() - fSuffix + 1 < fExpected.length() - fContextLength ? ELLIPSIS : "");
    }

    @SuppressWarnings("nullness")
    private boolean areStringsEqual() {
        // [dereference.of.nullable] FALSE_POSITIVE
        //  fExpected is ensured non-null here
        // the caller compact() calls this method in the if-else condition
        // if areStringEqual() is executed, it must be the case fExpected
        // and fActual are non-null
        return fExpected.equals(fActual);
    }
}
