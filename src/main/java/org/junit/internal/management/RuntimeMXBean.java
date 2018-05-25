package org.junit.internal.management;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Wrapper for {@link java.lang.management.RuntimeMXBean}.
 */
public interface RuntimeMXBean {

  /**
   * @see java.lang.management.RuntimeMXBean#getInputArguments()
   */
  @NotNull
  List<String> getInputArguments();
}
