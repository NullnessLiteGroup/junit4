package org.junit.tests.description;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.Description;

public class SuiteDescriptionTest {
    @NotNull
    Description childless = Description.createSuiteDescription("a");
    @NotNull
    Description anotherChildless = Description.createSuiteDescription("a");
    @NotNull
    Description namedB = Description.createSuiteDescription("b");

    @NotNull
    Description twoKids = descriptionWithTwoKids("foo", "bar");
    @NotNull
    Description anotherTwoKids = descriptionWithTwoKids("foo", "baz");

    @Test
    public void equalsIsCorrect() {
        assertEquals(childless, anotherChildless);
        assertFalse(childless.equals(namedB));
        assertEquals(childless, twoKids);
        assertEquals(twoKids, anotherTwoKids);
        assertFalse(twoKids.equals(new Integer(5)));
    }

    @Test
    public void hashCodeIsReasonable() {
        assertEquals(childless.hashCode(), anotherChildless.hashCode());
        assertFalse(childless.hashCode() == namedB.hashCode());
    }

    @NotNull
    private Description descriptionWithTwoKids(String first, String second) {
        Description twoKids = Description.createSuiteDescription("a");
        twoKids.addChild(Description.createTestDescription(getClass(), first));
        twoKids.addChild(Description.createTestDescription(getClass(), second));
        return twoKids;
    }
}
