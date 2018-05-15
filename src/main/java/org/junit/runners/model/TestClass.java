package org.junit.runners.model;

import static java.lang.reflect.Modifier.isStatic;
import static org.junit.internal.MethodSorter.NAME_ASCENDING;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.internal.MethodSorter;

/**
 * Wraps a class to be run, providing method validation and annotation searching
 *
 * @since 4.5
 */
public class TestClass implements Annotatable {
    private static final FieldComparator FIELD_COMPARATOR = new FieldComparator();
    private static final MethodComparator METHOD_COMPARATOR = new MethodComparator();

    @Nullable
    private final Class<?> clazz;
    @NotNull
    private final Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations;
    @NotNull
    private final Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations;

    /**
     * Creates a {@code TestClass} wrapping {@code clazz}. Each time this
     * constructor executes, the class is scanned for annotations, which can be
     * an expensive process (we hope in future JDK's it will not be.) Therefore,
     * try to share instances of {@code TestClass} where possible.
     */
    public TestClass(@Nullable Class<?> clazz) {
        this.clazz = clazz;
        if (clazz != null && clazz.getConstructors().length > 1) {
            throw new IllegalArgumentException(
                    "Test class can only have one constructor");
        }

        @NotNull Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations =
                new LinkedHashMap<Class<? extends Annotation>, List<FrameworkMethod>>();
        @NotNull Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations =
                new LinkedHashMap<Class<? extends Annotation>, List<FrameworkField>>();

        scanAnnotatedMembers(methodsForAnnotations, fieldsForAnnotations);

        this.methodsForAnnotations = makeDeeplyUnmodifiable(methodsForAnnotations);
        this.fieldsForAnnotations = makeDeeplyUnmodifiable(fieldsForAnnotations);
    }


    protected void scanAnnotatedMembers(@NotNull Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations, @NotNull Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations) {
        for (@NotNull Class<?> eachClass : getSuperClasses(clazz)) {
            for (Method eachMethod : MethodSorter.getDeclaredMethods(eachClass)) {
                addToAnnotationLists(new FrameworkMethod(eachMethod), methodsForAnnotations);
            }
            // ensuring fields are sorted to make sure that entries are inserted
            // and read from fieldForAnnotations in a deterministic order
            for (Field eachField : getSortedDeclaredFields(eachClass)) {
                addToAnnotationLists(new FrameworkField(eachField), fieldsForAnnotations);
            }
        }
    }

    private static Field[] getSortedDeclaredFields(Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        Arrays.sort(declaredFields, FIELD_COMPARATOR);
        return declaredFields;
    }

    protected static <T extends FrameworkMember<T>> void addToAnnotationLists(T member,
                                                                              @NotNull Map<Class<? extends Annotation>, List<T>> map) {
        for (@NotNull Annotation each : member.getAnnotations()) {
            Class<? extends Annotation> type = each.annotationType();
            @NotNull List<T> members = getAnnotatedMembers(map, type, true);
            @Nullable T memberToAdd = member.handlePossibleShadowedMember(members);
            if (memberToAdd == null) {
                return;
            }
            if (runsTopToBottom(type)) {
                members.add(0, memberToAdd);
            } else {
                members.add(memberToAdd);
            }
        }
    }

    private static <T extends FrameworkMember<T>> Map<Class<? extends Annotation>, List<T>>
            makeDeeplyUnmodifiable(Map<Class<? extends Annotation>, List<T>> source) {
        @NotNull Map<Class<? extends Annotation>, List<T>> copy =
                new LinkedHashMap<Class<? extends Annotation>, List<T>>();
        for (@NotNull Map.Entry<Class<? extends Annotation>, List<T>> entry : source.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Returns, efficiently, all the non-overridden methods in this class and
     * its superclasses that are annotated}.
     * 
     * @since 4.12
     */
    @NotNull
    public List<FrameworkMethod> getAnnotatedMethods() {
        @NotNull List<FrameworkMethod> methods = collectValues(methodsForAnnotations);
        Collections.sort(methods, METHOD_COMPARATOR);
        return methods;
    }

    /**
     * Returns, efficiently, all the non-overridden methods in this class and
     * its superclasses that are annotated with {@code annotationClass}.
     */
    @NotNull
    public List<FrameworkMethod> getAnnotatedMethods(
            Class<? extends Annotation> annotationClass) {
        return Collections.unmodifiableList(getAnnotatedMembers(methodsForAnnotations, annotationClass, false));
    }

    /**
     * Returns, efficiently, all the non-overridden fields in this class and its
     * superclasses that are annotated.
     * 
     * @since 4.12
     */
    @NotNull
    public List<FrameworkField> getAnnotatedFields() {
        return collectValues(fieldsForAnnotations);
    }

    /**
     * Returns, efficiently, all the non-overridden fields in this class and its
     * superclasses that are annotated with {@code annotationClass}.
     */
    @NotNull
    public List<FrameworkField> getAnnotatedFields(
            Class<? extends Annotation> annotationClass) {
        return Collections.unmodifiableList(getAnnotatedMembers(fieldsForAnnotations, annotationClass, false));
    }

    private <T> List<T> collectValues(Map<?, List<T>> map) {
        @NotNull Set<T> values = new LinkedHashSet<T>();
        for (@NotNull List<T> additionalValues : map.values()) {
            values.addAll(additionalValues);
        }
        return new ArrayList<T>(values);
    }

    @NotNull
    private static <T> List<T> getAnnotatedMembers(Map<Class<? extends Annotation>, List<T>> map,
                                                   Class<? extends Annotation> type, boolean fillIfAbsent) {
        if (!map.containsKey(type) && fillIfAbsent) {
            map.put(type, new ArrayList<T>());
        }
        List<T> members = map.get(type);
        return members == null ? Collections.<T>emptyList() : members;
    }

    private static boolean runsTopToBottom(Class<? extends Annotation> annotation) {
        return annotation.equals(Before.class)
                || annotation.equals(BeforeClass.class);
    }

    @NotNull
    private static List<Class<?>> getSuperClasses(Class<?> testClass) {
        @NotNull List<Class<?>> results = new ArrayList<Class<?>>();
        Class<?> current = testClass;
        while (current != null) {
            results.add(current);
            current = current.getSuperclass();
        }
        return results;
    }

    /**
     * Returns the underlying Java class.
     */
    @Nullable
    public Class<?> getJavaClass() {
        return clazz;
    }

    /**
     * Returns the class's name.
     */
    public String getName() {
        if (clazz == null) {
            return "null";
        }
        return clazz.getName();
    }

    /**
     * Returns the only public constructor in the class, or throws an {@code
     * AssertionError} if there are more or less than one.
     */

    public Constructor<?> getOnlyConstructor() {
        Constructor<?>[] constructors = clazz.getConstructors();
        /*
          This is a true positive. "clazz" might be null (we know it from both its annotation (line 38) and
          its initialization (line 51~52)), and thus class.getConstructors() might raise NullPointerException.
         */
        Assert.assertEquals(1, constructors.length);
        return constructors[0];
    }

    /**
     * Returns the annotations on this class
     */
    public Annotation[] getAnnotations() {
        if (clazz == null) {
            return new Annotation[0];
        }
        return clazz.getAnnotations();
    }

    @Nullable
    public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationType) {
        if (clazz == null) {
            return null;
        }
        return clazz.getAnnotation(annotationType);
    }

    @NotNull
    public <T> List<T> getAnnotatedFieldValues(Object test,
                                               Class<? extends Annotation> annotationClass, @NotNull Class<T> valueClass) {
        @NotNull final List<T> results = new ArrayList<T>();
        collectAnnotatedFieldValues(test, annotationClass, valueClass,
                new MemberValueConsumer<T>() {
                    public void accept(FrameworkMember member, T value) {
                        results.add(value);
                    }
                });
        return results;
    }

    /**
     * Finds the fields annotated with the specified annotation and having the specified type,
     * retrieves the values and passes those to the specified consumer.
     *
     * @since 4.13
     */
    public <T> void collectAnnotatedFieldValues(Object test,
                                                Class<? extends Annotation> annotationClass, @NotNull Class<T> valueClass,
                                                @NotNull MemberValueConsumer<T> consumer) {
        for (@NotNull FrameworkField each : getAnnotatedFields(annotationClass)) {
            try {
                Object fieldValue = each.get(test);
                if (valueClass.isInstance(fieldValue)) {
                    consumer.accept(each, valueClass.cast(fieldValue));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        "How did getFields return a field we couldn't access?", e);
            }
        }
    }

    @NotNull
    public <T> List<T> getAnnotatedMethodValues(Object test,
                                                Class<? extends Annotation> annotationClass, @NotNull Class<T> valueClass) {
        @NotNull final List<T> results = new ArrayList<T>();
        collectAnnotatedMethodValues(test, annotationClass, valueClass,
                new MemberValueConsumer<T>() {
                    public void accept(FrameworkMember member, T value) {
                        results.add(value);
                    }
                });
        return results;
    }

    /**
     * Finds the methods annotated with the specified annotation and returning the specified type,
     * invokes it and pass the return value to the specified consumer.
     *
     * @since 4.13
     */
    public <T> void collectAnnotatedMethodValues(Object test,
                                                 Class<? extends Annotation> annotationClass, @NotNull Class<T> valueClass,
                                                 @NotNull MemberValueConsumer<T> consumer) {
        for (@NotNull FrameworkMethod each : getAnnotatedMethods(annotationClass)) {
            try {
                /*
                 * A method annotated with @Rule may return a @TestRule or a @MethodRule,
                 * we cannot call the method to check whether the return type matches our
                 * expectation i.e. subclass of valueClass. If we do that then the method 
                 * will be invoked twice and we do not want to do that. So we first check
                 * whether return type matches our expectation and only then call the method
                 * to fetch the MethodRule
                 */
                if (valueClass.isAssignableFrom(each.getReturnType())) {
                    Object fieldValue = each.invokeExplosively(test);
                    consumer.accept(each, valueClass.cast(fieldValue));
                }
            } catch (Throwable e) {
                throw new RuntimeException(
                        "Exception in " + each.getName(), e);
            }
        }
    }

    public boolean isPublic() {
        return Modifier.isPublic(clazz.getModifiers());
        /*
          This is a true positive. "clazz" might be null (we know it from both its annotation (line 38) and
          its initialization (line 51~52)), and thus class.getConstructors() might raise NullPointerException.
         */
    }

    public boolean isANonStaticInnerClass() {
        return clazz.isMemberClass() && !isStatic(clazz.getModifiers());
        /*
          This is a true positive. "clazz" might be null (we know it from both its annotation (line 38) and
          its initialization (line 51~52)), and thus class.getConstructors() might raise NullPointerException.
         */
    }

    @Override
    public int hashCode() {
        return (clazz == null) ? 0 : clazz.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @Nullable TestClass other = (TestClass) obj;
        return clazz == other.clazz;
    }

    /**
     * Compares two fields by its name.
     */
    private static class FieldComparator implements Comparator<Field> {
        public int compare(@NotNull Field left, @NotNull Field right) {
            return left.getName().compareTo(right.getName());
        }
    }

    /**
     * Compares two methods by its name.
     */
    private static class MethodComparator implements
            Comparator<FrameworkMethod> {
        public int compare(@NotNull FrameworkMethod left, @NotNull FrameworkMethod right) {
            return NAME_ASCENDING.compare(left.getMethod(), right.getMethod());
        }
    }
}
