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
    @NotNull
    public Filter createFilter(@NotNull FilterFactoryParams params) throws FilterNotCreatedException {
        try {
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
    @NotNull
    protected abstract Filter createFilter(List<Class<?>> categories);

    @NotNull
    private List<Class<?>> parseCategories(String categories) throws ClassNotFoundException {
        List<Class<?>> categoryClasses = new ArrayList<Class<?>>();

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
