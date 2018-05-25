package org.junit.internal.builders;

import org.jetbrains.annotations.NotNull;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class IgnoredClassRunner extends Runner {
    private final Class<?> clazz;

    public IgnoredClassRunner(Class<?> testClass) {
        clazz = testClass;
    }

    @Override
    public void run(@NotNull RunNotifier notifier) {
        notifier.fireTestIgnored(getDescription());
    }

    @NotNull
    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(clazz);
    }
}