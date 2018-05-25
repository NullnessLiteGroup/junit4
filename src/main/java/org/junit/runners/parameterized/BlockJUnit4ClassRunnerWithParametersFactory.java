package org.junit.runners.parameterized;

import org.jetbrains.annotations.NotNull;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;

/**
 * A {@link ParametersRunnerFactory} that creates
 * {@link BlockJUnit4ClassRunnerWithParameters}.
 * 
 * @since 4.12
 */
public class BlockJUnit4ClassRunnerWithParametersFactory implements
        ParametersRunnerFactory {
    @NotNull
    public Runner createRunnerForTestWithParameters(@NotNull TestWithParameters test)
            throws InitializationError {
        return new BlockJUnit4ClassRunnerWithParameters(test);
    }
}
