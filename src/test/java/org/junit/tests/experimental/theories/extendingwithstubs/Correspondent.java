package org.junit.tests.experimental.theories.extendingwithstubs;

import org.jetbrains.annotations.NotNull;

public interface Correspondent {

    @NotNull
    String getAnswer(String question, String... bucket);

}
