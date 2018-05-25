package org.junit.tests.experimental.theories.extendingwithstubs;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.internal.AssumptionViolatedException;

public abstract class ReguessableValue extends PotentialAssignment {

    public ReguessableValue() {
        super();
    }

    @NotNull
    public abstract List<ReguessableValue> reguesses(
            AssumptionViolatedException e);
}