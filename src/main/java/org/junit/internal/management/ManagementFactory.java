package org.junit.internal.management;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.internal.Classes;

import java.lang.reflect.InvocationTargetException;

/**
 * Reflective wrapper around {@link java.lang.management.ManagementFactory}
 */
public class ManagementFactory {
  private static final class FactoryHolder {
    @Nullable
    private static final Class<?> MANAGEMENT_FACTORY_CLASS;

    static {
      @Nullable Class<?> managementFactoryClass = null;
      try {
        managementFactoryClass = Classes.getClass("java.lang.management.ManagementFactory");
      } catch (ClassNotFoundException e) {
        // do nothing, managementFactoryClass will be none on failure
      }
      MANAGEMENT_FACTORY_CLASS = managementFactoryClass;
    }

    static Object getBeanObject(@NotNull String methodName) {
      if (MANAGEMENT_FACTORY_CLASS != null) {
        try {
          return MANAGEMENT_FACTORY_CLASS.getMethod(methodName).invoke(null);
        } catch (IllegalAccessException e) {
          // fallthrough
        } catch (IllegalArgumentException e) {
          // fallthrough
        } catch (InvocationTargetException e) {
          // fallthrough
        } catch (NoSuchMethodException e) {
          // fallthrough
        } catch (SecurityException e) {
          // fallthrough
        }
      }
      return null;
    }
  }

  private static final class RuntimeHolder {
    private static final RuntimeMXBean RUNTIME_MX_BEAN =
        getBean(FactoryHolder.getBeanObject("getRuntimeMXBean"));

    private static final RuntimeMXBean getBean(@Nullable Object runtimeMxBean) {
      return runtimeMxBean != null
          ? new ReflectiveRuntimeMXBean(runtimeMxBean) : new FakeRuntimeMXBean();
    }
  }

  private static final class ThreadHolder {
    private static final ThreadMXBean THREAD_MX_BEAN =
        getBean(FactoryHolder.getBeanObject("getThreadMXBean"));

    private static final ThreadMXBean getBean(@Nullable Object threadMxBean) {
      return threadMxBean != null
          ? new ReflectiveThreadMXBean(threadMxBean) : new FakeThreadMXBean();
    }
  }

  /**
   * @see java.lang.management.ManagementFactory#getRuntimeMXBean()
   */
  @NotNull
  public static RuntimeMXBean getRuntimeMXBean() {
    return RuntimeHolder.RUNTIME_MX_BEAN;
  }

  /**
   * @see java.lang.management.ManagementFactory#getThreadMXBean()
   */
  @NotNull
  public static ThreadMXBean getThreadMXBean() {
    return ThreadHolder.THREAD_MX_BEAN;
  }
}
