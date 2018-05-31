package org.junit.internal.management;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.Classes;

import java.lang.reflect.InvocationTargetException;

/**
 * Reflective wrapper around {@link java.lang.management.ManagementFactory}
 */
@SuppressWarnings("nullness")
public class ManagementFactory {
  private static final class FactoryHolder {
    private static final Class<?> MANAGEMENT_FACTORY_CLASS;

    static {
      Class<?> managementFactoryClass = null;
      try {
        managementFactoryClass = Classes.getClass("java.lang.management.ManagementFactory");
      } catch (ClassNotFoundException e) {
        // do nothing, managementFactoryClass will be none on failure
      }
      // [assignment.type.incompatible] FALSE_POSITIVE
      // java.lang.management.ManagementFactory class exist
      // so it can never be null
      MANAGEMENT_FACTORY_CLASS = managementFactoryClass;
    }

    // Nullable Object returned when invoke returns null
    static Object getBeanObject(String methodName) {
      if (MANAGEMENT_FACTORY_CLASS != null) {
        try {
          // [return.type.incompatible] FALSE_POSITIVE
          // this class is not exposed in JUnit4 to clients;
          // the calls from this project is not malformed;
          // MANAGEMENT_FACTORY_CLASS always exists, and has methods
          // getThreadMXBean() and getRuntimeMXBean() for calls from this project
          // and both of them return the non-null object
          // @see ava.lang.management.ManagementFactory#getThreadMXBean()
          // @see ava.lang.management.ManagementFactory#getRuntimeMXBean()
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
      // [return.type.incompatible] FALSE_POSITIVE
      // this class is not exposed in JUnit4 to clients;
      // the calls from this project is not malformed;
      // MANAGEMENT_FACTORY_CLASS always exists, and has methods
      // getThreadMXBean() and getRuntimeMXBean() for calls from this project
      // and both of them return the non-null object
      // so we never reach this line
      // @see ava.lang.management.ManagementFactory#getThreadMXBean()
      // @see ava.lang.management.ManagementFactory#getRuntimeMXBean()
      return null;
    }
  }

  private static final class RuntimeHolder {
    private static final RuntimeMXBean RUNTIME_MX_BEAN =
        getBean(FactoryHolder.getBeanObject("getRuntimeMXBean"));

    // Nullable runtimeMxBean indicated from implementation
    private static final RuntimeMXBean getBean(@Nullable Object runtimeMxBean) {
      return runtimeMxBean != null
          ? new ReflectiveRuntimeMXBean(runtimeMxBean) : new FakeRuntimeMXBean();
    }
  }

  private static final class ThreadHolder {
    private static final ThreadMXBean THREAD_MX_BEAN =
        getBean(FactoryHolder.getBeanObject("getThreadMXBean"));

    //  @Nullable threadMxBean indicated from implementation
    private static final ThreadMXBean getBean(@Nullable Object threadMxBean) {
      return threadMxBean != null
          ? new ReflectiveThreadMXBean(threadMxBean) : new FakeThreadMXBean();
    }
  }

  /**
   * @see java.lang.management.ManagementFactory#getRuntimeMXBean()
   */
  public static RuntimeMXBean getRuntimeMXBean() {
    return RuntimeHolder.RUNTIME_MX_BEAN;
  }

  /**
   * @see java.lang.management.ManagementFactory#getThreadMXBean()
   */
  public static ThreadMXBean getThreadMXBean() {
    return ThreadHolder.THREAD_MX_BEAN;
  }
}
