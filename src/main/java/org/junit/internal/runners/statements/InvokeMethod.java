package org.junit.internal.runners.statements;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class InvokeMethod extends Statement {
    private final FrameworkMethod testMethod;
    private final @Nullable Object target;

    public InvokeMethod(FrameworkMethod testMethod, @Nullable Object target) {
        this.testMethod = testMethod;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        testMethod.invokeExplosively(target);
    }
}