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
    public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
        Object[] enumValues = enumType.getEnumConstants();
        
        List<PotentialAssignment> assignments = new ArrayList<PotentialAssignment>();
        // [iterating.over.nullable] TRUE_POSITIVE
        // iterating over possibly-null reference enumValues
        for (Object value : enumValues) {
            assignments.add(PotentialAssignment.forValue(value.toString(), value));
        }
        
        return assignments;
    }

}
