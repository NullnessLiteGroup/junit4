package org.junit.experimental.theories;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterSignature {
    
    private static final Map<Class<?>, Class<?>> CONVERTABLE_TYPES_MAP = buildConvertableTypesMap();
    
    private static Map<Class<?>, Class<?>> buildConvertableTypesMap() {
        Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();

        putSymmetrically(map, boolean.class, Boolean.class);
        putSymmetrically(map, byte.class, Byte.class);
        putSymmetrically(map, short.class, Short.class);
        putSymmetrically(map, char.class, Character.class);
        putSymmetrically(map, int.class, Integer.class);
        putSymmetrically(map, long.class, Long.class);
        putSymmetrically(map, float.class, Float.class);
        putSymmetrically(map, double.class, Double.class);

        return Collections.unmodifiableMap(map);
    }
    
    private static <T> void putSymmetrically(Map<T, T> map, T a, T b) {
        map.put(a, b);
        map.put(b, a);
    }
    
    @NotNull
    public static ArrayList<ParameterSignature> signatures(Method method) {
        return signatures(method.getParameterTypes(), method
                .getParameterAnnotations());
    }

    @NotNull
    public static List<ParameterSignature> signatures(Constructor<?> constructor) {
        return signatures(constructor.getParameterTypes(), constructor
                .getParameterAnnotations());
    }

    @NotNull
    private static ArrayList<ParameterSignature> signatures(
            Class<?>[] parameterTypes, Annotation[][] parameterAnnotations) {
        ArrayList<ParameterSignature> sigs = new ArrayList<ParameterSignature>();
        for (int i = 0; i < parameterTypes.length; i++) {
            sigs.add(new ParameterSignature(parameterTypes[i],
                    parameterAnnotations[i]));
        }
        return sigs;
    }

    private final Class<?> type;

    private final Annotation[] annotations;

    private ParameterSignature(Class<?> type, Annotation[] annotations) {
        this.type = type;
        this.annotations = annotations;
    }

    public boolean canAcceptValue(@Nullable Object candidate) {
        return (candidate == null) ? !type.isPrimitive() : canAcceptType(candidate.getClass());
    }

    public boolean canAcceptType(@NotNull Class<?> candidate) {
        return type.isAssignableFrom(candidate) ||
                isAssignableViaTypeConversion(type, candidate);
    }
    
    public boolean canPotentiallyAcceptType(@NotNull Class<?> candidate) {
        return candidate.isAssignableFrom(type) ||
                isAssignableViaTypeConversion(candidate, type) ||
                canAcceptType(candidate);
    }

    private boolean isAssignableViaTypeConversion(@NotNull Class<?> targetType, Class<?> candidate) {
        if (CONVERTABLE_TYPES_MAP.containsKey(candidate)) {
            Class<?> wrapperClass = CONVERTABLE_TYPES_MAP.get(candidate);
            return targetType.isAssignableFrom(wrapperClass);
        } else {
            return false;
        }
    }

    public Class<?> getType() {
        return type;
    }

    @NotNull
    public List<Annotation> getAnnotations() {
        return Arrays.asList(annotations);
    }

    public boolean hasAnnotation(@NotNull Class<? extends Annotation> type) {
        return getAnnotation(type) != null;
    }

    @Nullable
    public <T extends Annotation> T findDeepAnnotation(@NotNull Class<T> annotationType) {
        Annotation[] annotations2 = annotations;
        return findDeepAnnotation(annotations2, annotationType, 3);
    }

    private <T extends Annotation> T findDeepAnnotation(
            @NotNull Annotation[] annotations, @NotNull Class<T> annotationType, int depth) {
        if (depth == 0) {
            return null;
        }
        for (Annotation each : annotations) {
            if (annotationType.isInstance(each)) {
                return annotationType.cast(each);
            }
            Annotation candidate = findDeepAnnotation(each.annotationType()
                    .getAnnotations(), annotationType, depth - 1);
            if (candidate != null) {
                return annotationType.cast(candidate);
            }
        }

        return null;
    }

    @Nullable
    public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationType) {
        for (Annotation each : getAnnotations()) {
            if (annotationType.isInstance(each)) {
                return annotationType.cast(each);
            }
        }
        return null;
    }
}