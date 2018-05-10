package org.junit.experimental.theories.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    
    @Override
    @SuppressWarnings("nullness")
    protected Collection<Field> getSingleDataPointFields(ParameterSignature sig) {
        Collection<Field> fields = super.getSingleDataPointFields(sig);
        // [dereference.of.nullable] TRUE_POSITIVE
        // dereference of possibly-null reference sig.getAnnotation(FromDataPoints.class)
        // sig.getAnnotation() may return null if not found
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();

        List<Field> fieldsWithMatchingNames = new ArrayList<Field>();
        
        for (Field field : fields) {
            // [dereference.of.nullable] TRUE_POSITIVE
            // dereference of possibly-null reference sig.getAnnotation(DataPoint.class)
            // sig.getAnnotation() may return null if not found
            String[] fieldNames = field.getAnnotation(DataPoint.class).value();
            if (Arrays.asList(fieldNames).contains(requestedName)) {
                fieldsWithMatchingNames.add(field);
            }
        }
        
        return fieldsWithMatchingNames;
    }
    
    @Override
    @SuppressWarnings("nullness")
    protected Collection<Field> getDataPointsFields(ParameterSignature sig) {
        Collection<Field> fields = super.getDataPointsFields(sig);
        // [dereference.of.nullable] TRUE_POSITIVE
        // dereference of possibly-null reference sig.getAnnotation(FromDataPoints.class)
        // sig.getAnnotation() may return null if not found
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();
        
        List<Field> fieldsWithMatchingNames = new ArrayList<Field>();
        
        for (Field field : fields) {
            // [dereference.of.nullable] TRUE_POSITIVE
            // dereference of possibly-null reference sig.getAnnotation(DataPoint.class)
            // sig.getAnnotation() may return null if not found
            String[] fieldNames = field.getAnnotation(DataPoints.class).value();
            if (Arrays.asList(fieldNames).contains(requestedName)) {
                fieldsWithMatchingNames.add(field);
            }
        }
        
        return fieldsWithMatchingNames;
    }
    
    @Override
    @SuppressWarnings("nullness")
    protected Collection<FrameworkMethod> getSingleDataPointMethods(ParameterSignature sig) {
        Collection<FrameworkMethod> methods = super.getSingleDataPointMethods(sig);
        // [dereference.of.nullable] TRUE_POSITIVE
        // dereference of possibly-null reference sig.getAnnotation(FromDataPoints.class)
        // sig.getAnnotation() may return null if not found
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();
        
        List<FrameworkMethod> methodsWithMatchingNames = new ArrayList<FrameworkMethod>();
        
        for (FrameworkMethod method : methods) {
            // [dereference.of.nullable] TRUE_POSITIVE
            // dereference of possibly-null reference sig.getAnnotation(DataPoint.class)
            // sig.getAnnotation() may return null if not found
            String[] methodNames = method.getAnnotation(DataPoint.class).value();
            if (Arrays.asList(methodNames).contains(requestedName)) {
                methodsWithMatchingNames.add(method);
            }
        }
        
        return methodsWithMatchingNames;
    }
    
    @Override
    @SuppressWarnings("nullness")
    protected Collection<FrameworkMethod> getDataPointsMethods(ParameterSignature sig) {
        Collection<FrameworkMethod> methods = super.getDataPointsMethods(sig);
        // [dereference.of.nullable] TRUE_POSITIVE
        // dereference of possibly-null reference sig.getAnnotation(FromDataPoints.class)
        // sig.getAnnotation() may return null if not found
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();
        
        List<FrameworkMethod> methodsWithMatchingNames = new ArrayList<FrameworkMethod>();
        
        for (FrameworkMethod method : methods) {
            // [dereference.of.nullable] TRUE_POSITIVE
            // dereference of possibly-null reference sig.getAnnotation(DataPoints.class)
            // sig.getAnnotation() may return null if not found
            String[] methodNames = method.getAnnotation(DataPoints.class).value();
            if (Arrays.asList(methodNames).contains(requestedName)) {
                methodsWithMatchingNames.add(method);
            }
        }
        
        return methodsWithMatchingNames;
    }

}
