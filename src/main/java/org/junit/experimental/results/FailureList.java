package org.junit.experimental.results;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

class FailureList {
    private final List<Failure> failures;

    public FailureList(List<Failure> failures) {
        this.failures = failures;
    }

    @NotNull
    public Result result() {
        @NotNull Result result = new Result();
        @NotNull RunListener listener = result.createListener();
        for (Failure failure : failures) {
            try {
                listener.testFailure(failure);
            } catch (Exception e) {
                throw new RuntimeException("I can't believe this happened");
            }
        }
        return result;
    }
}