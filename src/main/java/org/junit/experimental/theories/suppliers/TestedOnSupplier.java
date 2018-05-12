package org.junit.experimental.theories.suppliers;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;

/**
 * @see org.junit.experimental.theories.suppliers.TestedOn
 * @see org.junit.experimental.theories.ParameterSupplier
 */
public class TestedOnSupplier extends ParameterSupplier {
    @NotNull
    @Override
    public List<PotentialAssignment> getValueSources(@NotNull ParameterSignature sig) {
        @NotNull List<PotentialAssignment> list = new ArrayList<PotentialAssignment>();
        TestedOn testedOn = sig.getAnnotation(TestedOn.class);
        @NotNull int[] ints = testedOn.ints();
        for (final int i : ints) {
            list.add(PotentialAssignment.forValue("ints", i));
        }
        return list;
    }
}
