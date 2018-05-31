package org.junit.experimental.categories;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.internal.Classes;
import org.junit.runner.FilterFactory;
import org.junit.runner.FilterFactoryParams;
import org.junit.runner.manipulation.Filter;

/**
 * Implementation of FilterFactory for Category filtering.
 */
abstract class CategoryFilterFactory implements FilterFactory {
    /**
     * Creates a {@link org.junit.experimental.categories.Categories.CategoryFilter} given a
     * {@link FilterFactoryParams} argument.
     *
     * @param params Parameters needed to create the {@link Filter}
     */
    public Filter createFilter(@NotNull FilterFactoryParams params) throws FilterNotCreatedException {
        try {
            /*
               [FALSE_POSITIVE]
               This is a false positive. By looking at the implementation of params.getArgs(),
               we know that it returns the field "args" of a FilterFactoryParams instance
               (src/main/java/org/junit/runner/FilterFactoryParams.java).
               However, this "args" field can never be null, because the constructor (FilterFactoryParams.java: line 11)
               checks the parameter:
               if the parameter "args" is null, it throws an exception; otherwise, it assigns the parameter
               to its field, "args". Therefore, without an exception being thrown, the field "args" won't be
               null, which means params.getArgs() will never be null.
             */
            return createFilter(parseCategories(params.getArgs()));
        } catch (ClassNotFoundException e) {
            throw new FilterNotCreatedException(e);
        }
    }

    /**
     * Creates a {@link org.junit.experimental.categories.Categories.CategoryFilter} given an array of classes.
     *
     * @param categories Category classes.
     */
    protected abstract Filter createFilter(List<Class<?>> categories);

    @NotNull
    private List<Class<?>> parseCategories(String categories) throws ClassNotFoundException {
        @NotNull List<Class<?>> categoryClasses = new ArrayList<Class<?>>();

        for (String category : categories.split(",")) {
            /*
             * Load the category class using the context class loader.
             * If there is no context class loader, use the class loader for this class.
             */
            Class<?> categoryClass = Classes.getClass(category, getClass());

            categoryClasses.add(categoryClass);
        }

        return categoryClasses;
    }
}
