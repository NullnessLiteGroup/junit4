package org.junit.internal.runners.statements;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

public class RunAfters extends Statement {
    private final Statement next;

    private final Object target;

    private final List<FrameworkMethod> afters;

    // @Nullable target from ParentRunner: withAfterClasses(Statement statement)
    public RunAfters(Statement next, List<FrameworkMethod> afters, @Nullable Object target) {
        this.next = next;
        this.afters = afters;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        List<Throwable> errors = new ArrayList<Throwable>();
        try {
            next.evaluate();
        } catch (Throwable e) {
            errors.add(e);
        } finally {
            for (FrameworkMethod each : afters) {
                try {
                    invokeMethod(each);
                } catch (Throwable e) {
                    errors.add(e);
                }
            }
        }
        MultipleFailureException.assertEmpty(errors);
    }

    /**
     * @since 4.13
     */
    protected void invokeMethod(FrameworkMethod method) throws Throwable {
        method.invokeExplosively(target);
    }
}