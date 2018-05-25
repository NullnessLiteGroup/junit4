package org.junit.runner;

import org.jetbrains.annotations.Nullable;

public final class FilterFactoryParams {
    @Nullable
    private final Description topLevelDescription;
    @Nullable
    private final String args;

    public FilterFactoryParams(@Nullable Description topLevelDescription, @Nullable String args) {
        if (args == null || topLevelDescription == null) {
            throw new NullPointerException();
        }

        this.topLevelDescription = topLevelDescription;
        this.args = args;
    }

    @Nullable
    public String getArgs() {
        return args;
    }

    @Nullable
    public Description getTopLevelDescription() {
        return topLevelDescription;
    }
}
