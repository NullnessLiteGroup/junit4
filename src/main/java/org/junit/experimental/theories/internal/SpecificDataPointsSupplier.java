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
        // [dereference.of.nullable] FALSE_POSITIVE
        //  dereference of getAnnotation(FromDataPoints.class) is safe here
        // since annotation DataPoints will only be considered effective,
        // checked in getDataPointsFields(sig),
        // if it is paired with annotation FromDataPoints
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();

        List<Field> fieldsWithMatchingNames = new ArrayList<Field>();
        
        for (Field field : fields) {
            // [dereference.of.nullable] FALSE_POSITIVE
            //   dereference getAnnotation(DataPoint.class) is same here
            // because we ensure that every field in fields have DataPoint.class
            // in getSingleDataPointFields(sig)
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
        // [dereference.of.nullable] FALSE_POSITIVE
        //  dereference of getAnnotation(FromDataPoints.class) is safe here
        // since annotation DataPoints will only be considered effective,
        // checked in getDataPointsFields(sig),
        // if it is paired with annotation FromDataPoints
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();
        
        List<Field> fieldsWithMatchingNames = new ArrayList<Field>();
        
        for (Field field : fields) {
            // [dereference.of.nullable] FALSE_POSITIVE
            //   dereference getAnnotation(DataPoint.class) is same here
            // because we ensure that every field in fields have DataPoint.class
            // in getDataPointsFields(sig)
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
        // [dereference.of.nullable] FALSE_POSITIVE
        //  dereference of getAnnotation(FromDataPoints.class) is safe here
        // since annotation DataPoints will only be considered effective,
        // checked in getSingleDataPointMethods(sig),
        // if it is paired with annotation FromDataPoints
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();
        
        List<FrameworkMethod> methodsWithMatchingNames = new ArrayList<FrameworkMethod>();
        
        for (FrameworkMethod method : methods) {
            // [dereference.of.nullable] FALSE_POSITIVE
            //   dereference getAnnotation(DataPoint.class) is same here
            // because we ensure that every method in methods have DataPoint.class
            // in getDataPointsMethods(sig)
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
        // [dereference.of.nullable] FALSE_POSITIVE
        //  dereference of getAnnotation(FromDataPoints.class) is safe here
        // since annotation DataPoints will only be considered effective,
        // checked in getDataPointsMethods(sig),
        // if it is paired with annotation FromDataPoints
        String requestedName = sig.getAnnotation(FromDataPoints.class).value();
        
        List<FrameworkMethod> methodsWithMatchingNames = new ArrayList<FrameworkMethod>();
        
        for (FrameworkMethod method : methods) {
            // [dereference.of.nullable] FALSE_POSITIVE
            //   dereference getAnnotation(DataPoint.class) is same here
            // because we ensure that every method in methods have DataPoint.class
            // in getDataPointsMethods(sig)
            String[] methodNames = method.getAnnotation(DataPoints.class).value();
            if (Arrays.asList(methodNames).contains(requestedName)) {
                methodsWithMatchingNames.add(method);
            }
        }
        
        return methodsWithMatchingNames;
    }

}
