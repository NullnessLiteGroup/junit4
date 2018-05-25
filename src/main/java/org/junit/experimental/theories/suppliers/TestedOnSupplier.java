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
        /*
          This is a true positive. We can see from the implementation of getAnnotation()
          (src/main/java/org/junit/experimental/theories/ParameterSignature.java: line 135)
          that if the field "annotations" (ParameterSignature.java: line 65) is initialized as
          an empty array (ParameterSignature.java: line 69),
          getAnnotations() (ParameterSignature.java: line 101) will return an empty ArrayList
          and then getAnnotation() will return null, which means testedOn will be null.
         */
        for (final int i : ints) {
            list.add(PotentialAssignment.forValue("ints", i));
        }
        return list;
    }
}
