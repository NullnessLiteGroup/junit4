package org.junit.experimental.theories;

import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.String.format;

public abstract class PotentialAssignment {
    public static class CouldNotGenerateValueException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public CouldNotGenerateValueException() {
        }
        
        public CouldNotGenerateValueException(Throwable e) {
            super(e);
        }
    }

    // Nullable valud from addSinglePointFields(ParameterSignature sig, List<PotentialAssignment> list)
    public static PotentialAssignment forValue(final String name, final @Nullable Object value) {
        return new PotentialAssignment() {
            @Override
            // Nullable Object returned if value is null
            public @Nullable Object getValue() {
                return value;
            }

            @Override
            public String toString() {
                return format("[%s]", value);
            }

            @Override
            public String getDescription() {
                String valueString;

                if (value == null) {
                    valueString = "null";
                } else {
                    try {
                        valueString = format("\"%s\"", value);
                    } catch (Throwable e) {
                        valueString = format("[toString() threw %s: %s]", 
                                             e.getClass().getSimpleName(), e.getMessage());
                    }
                }

                return format("%s <from %s>", valueString, name);
            }
        };
    }

    // Nullable Object returned from subclass
    public abstract @Nullable Object getValue() throws CouldNotGenerateValueException;

    public abstract String getDescription() throws CouldNotGenerateValueException;
}