package org.junit.tests.experimental.theories.extendingwithstubs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;

import org.jetbrains.annotations.NotNull;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(StubbedTheories.class)
public class StubbedTheoriesTest {
    @Theory
    public void ask(@NotNull @Stub Correspondent correspondent) {
        assumeThat(correspondent.getAnswer("What is five?", "four", "five"),
                is("five"));
    }
}
