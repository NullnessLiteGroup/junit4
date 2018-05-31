package org.junit.experimental.theories.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;

public class EnumSupplier extends ParameterSupplier {

    private Class<?> enumType;

    public EnumSupplier(Class<?> enumType) {
        this.enumType = enumType;
    }

    @Override
    @SuppressWarnings("nullness")
    public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
        Object[] enumValues = enumType.getEnumConstants();
        
        List<PotentialAssignment> assignments = new ArrayList<PotentialAssignment>();
        // [iterating.over.nullable] FALSE_POSITIVE
        // EnumSupplier is not exposed in JUnit4 API,
        // the only caller for the constructor ensures the enumType
        // is Enum type which is defined to have the values() method
        // invoked in enumType.getEnumConstants(),
        // thus getEnumConstants() cannot return null
        for (Object value : enumValues) {
            assignments.add(PotentialAssignment.forValue(value.toString(), value));
        }
        
        return assignments;
    }

}
