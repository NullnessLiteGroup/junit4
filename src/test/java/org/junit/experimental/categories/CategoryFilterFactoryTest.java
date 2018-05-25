package org.junit.experimental.categories;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.runner.Description.createSuiteDescription;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.junit.runner.FilterFactory;
import org.junit.runner.FilterFactoryParams;
import org.junit.runner.manipulation.Filter;

public class CategoryFilterFactoryTest {
    @NotNull
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @NotNull
    @Rule
    public TestName testName = new TestName();

    private final CategoryFilterFactory categoryFilterFactory = new CategoryFilterFactoryStub();

    @Test
    public void shouldCreateFilter() throws Exception {
        FilterFactoryParams params = new FilterFactoryParams(
                createSuiteDescription(testName.getMethodName()),
                CategoryFilterFactoryStub.class.getName());
        Filter filter = categoryFilterFactory.createFilter(params);

        assertThat(filter, instanceOf(DummyFilter.class));
    }

    @Test
    public void shouldThrowException() throws Exception {
        FilterFactoryParams params = new FilterFactoryParams(
                createSuiteDescription(testName.getMethodName()),
                "NonExistentFilter");

        expectedException.expect(FilterFactory.FilterNotCreatedException.class);

        categoryFilterFactory.createFilter(params);
    }

    private static class CategoryFilterFactoryStub extends CategoryFilterFactory {
        @NotNull
        @Override
        protected Filter createFilter(List<Class<?>> categories) {
            return new DummyFilter();
        }
    }

    private static class DummyFilter extends Filter {
        @Override
        public boolean shouldRun(Description description) {
            return false;
        }

        @Nullable
        @Override
        public String describe() {
            return null;
        }
    }
}
