package org.junit.experimental.theories.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class SpecificDataPointsSupplier extends AllMembersSupplier {

    public SpecificDataPointsSupplier(TestClass testClass) {
        super(testClass);
    }
    
    @NotNull
    @Override
    protected Collection<Field> getSingleDataPointFields(ParameterSignature sig) {
        Collection<Field> fields = super.getSingleDataPointFields(sig);
        // [FALSE_POSITIVE]
        // dereference of getAnnotation(FromDataPoints.class) is safe here
        // since annotation DataPoints will only be considered effective,
        // checked in getDataPointsFields(sig),
        // if it is paired with annotation FromDataPoints
        // the other errors in this class are also false positives (they are in the same pattern)
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();

        @NotNull List<Field> fieldsWithMatchingNames = new ArrayList<Field>();
        
        for (@NotNull Field field : fields) {
            String[] fieldNames = field.getAnnotation(DataPoint.class).value();
            if (Arrays.asList(fieldNames).contains(requestedName)) {
                fieldsWithMatchingNames.add(field);
            }
        }
        
        return fieldsWithMatchingNames;
    }
    
    @NotNull
    @Override
    protected Collection<Field> getDataPointsFields(@NotNull ParameterSignature sig) {
        Collection<Field> fields = super.getDataPointsFields(sig);        
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();  // [FALSE_POSITIVE]
        
        @NotNull List<Field> fieldsWithMatchingNames = new ArrayList<Field>();
        
        for (@NotNull Field field : fields) {
            String[] fieldNames = field.getAnnotation(DataPoints.class).value();
            if (Arrays.asList(fieldNames).contains(requestedName)) {
                fieldsWithMatchingNames.add(field);
            }
        }
        
        return fieldsWithMatchingNames;
    }
    
    @NotNull
    @Override
    protected Collection<FrameworkMethod> getSingleDataPointMethods(@NotNull ParameterSignature sig) {
        Collection<FrameworkMethod> methods = super.getSingleDataPointMethods(sig);
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();  // [FALSE_POSITIVE]
        
        @NotNull List<FrameworkMethod> methodsWithMatchingNames = new ArrayList<FrameworkMethod>();
        
        for (@NotNull FrameworkMethod method : methods) {
            String[] methodNames = method.getAnnotation(DataPoint.class).value();  // [FALSE_POSITIVE]
            if (Arrays.asList(methodNames).contains(requestedName)) {
                methodsWithMatchingNames.add(method);
            }
        }
        
        return methodsWithMatchingNames;
    }
    
    @NotNull
    @Override
    protected Collection<FrameworkMethod> getDataPointsMethods(@NotNull ParameterSignature sig) {
        Collection<FrameworkMethod> methods = super.getDataPointsMethods(sig);
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();  // [FALSE_POSITIVE]
        
        @NotNull List<FrameworkMethod> methodsWithMatchingNames = new ArrayList<FrameworkMethod>();
        
        for (@NotNull FrameworkMethod method : methods) {
            String[] methodNames = method.getAnnotation(DataPoints.class).value();  // [FALSE_POSITIVE]
            if (Arrays.asList(methodNames).contains(requestedName)) {
                methodsWithMatchingNames.add(method);
            }
        }
        
        return methodsWithMatchingNames;
    }

}
