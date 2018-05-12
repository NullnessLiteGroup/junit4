package org.junit.internal.management;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * No-op implementation of RuntimeMXBean when the platform doesn't provide it.
 */
class FakeRuntimeMXBean implements RuntimeMXBean {

  /**
   * {@inheritDoc}
   *
   * <p>Always returns an empty list.
   */
  @NotNull
  public List<String> getInputArguments() {
    return Collections.emptyList();
  }

}

