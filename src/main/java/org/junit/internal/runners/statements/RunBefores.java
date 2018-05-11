package org.junit.internal.runners.statements;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RunBefores extends Statement {
    private final Statement next;

    private final Object target;

    private final List<FrameworkMethod> befores;

    // @Nullable target from ParentRunner: withBeforeClasses(Statement statement)
    public RunBefores(Statement next, List<FrameworkMethod> befores, @Nullable Object target) {
        this.next = next;
        this.befores = befores;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        for (FrameworkMethod before : befores) {
            invokeMethod(before);
        }
        next.evaluate();
    }

    /**
     * @since 4.13
     */
    protected void invokeMethod(FrameworkMethod method) throws Throwable {
        method.invokeExplosively(target);
    }
}