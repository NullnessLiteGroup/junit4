// An extra test case for AllMembersSupplier.class
// When we run this class, we expect an java.lang.NullPointerException at
// org.junit.experimental.theories.internal.AllMembersSupplier.addIterableValues
package practice;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(Theories.class)
public class TestAllMembersSupplier {
    @DataPoints
    public static List<String> dataPoints = null;
    @Theory
    public void testList(List<String> list) { }
}
