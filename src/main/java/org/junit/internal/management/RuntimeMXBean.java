package org.junit.internal.management;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * Wrapper for {@link java.lang.management.RuntimeMXBean}.
 */
public interface RuntimeMXBean {

  /**
   * @see java.lang.management.RuntimeMXBean#getInputArguments()
   */
  @Nullable List<String> getInputArguments();
}
