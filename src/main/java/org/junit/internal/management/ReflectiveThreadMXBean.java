package org.junit.internal.management;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.internal.Classes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implementation of {@link ThreadMXBean} using the JVM reflectively.
 */
final class ReflectiveThreadMXBean implements ThreadMXBean {
  private final Object threadMxBean;


  private static final class Holder {
    static final Method getThreadCpuTimeMethod;
    static final Method isThreadCpuTimeSupportedMethod;

    private static final String FAILURE_MESSAGE = "Unable to access ThreadMXBean";

    static {
      Method threadCpuTime = null;
      Method threadCpuTimeSupported = null;
      try {
        Class<?> threadMXBeanClass = Classes.getClass("java.lang.management.ThreadMXBean");
        threadCpuTime = threadMXBeanClass.getMethod("getThreadCpuTime", long.class);
        threadCpuTimeSupported = threadMXBeanClass.getMethod("isThreadCpuTimeSupported");
      } catch (ClassNotFoundException e) {
        // do nothing, the methods will be null on failure
      } catch (NoSuchMethodException e) {
        // do nothing, the methods will be null on failure
      } catch (SecurityException e) {
        // do nothing, the methods will be null on failure
      }
      // [assignment.type.incompatible] FALSE_POSITIVE
      // getThreadCpuTimeMethod cannot be null because
      // java.lang.management.ThreadMXBean exists and has
      // the method getThreadCpuTime(long id)
      getThreadCpuTimeMethod = threadCpuTime;
      // [assignment.type.incompatible] FALSE_POSITIVE
      // getThreadCpuTimeMethod cannot be null because
      // java.lang.management.ThreadMXBean exists and has
      // the method isThreadCpuTimeSupported()
      isThreadCpuTimeSupportedMethod = threadCpuTimeSupported;
    }
  }

  ReflectiveThreadMXBean(Object threadMxBean) {
    super();
    this.threadMxBean = threadMxBean;
  }

  /**
   * {@inheritDoc}
   */
  public long getThreadCpuTime(long id) {
    if (Holder.getThreadCpuTimeMethod != null) {
      Exception error = null;
      try {
        // [unboxing.of.nullable] FALSE_POSITIVE
        // getThreadCpuTimeMethod = java.lang.management.ThreadMXBean: getThreadCpuTime
        // which always returns a non-null primitive type long
        return (Long) Holder.getThreadCpuTimeMethod.invoke(threadMxBean, id);
      } catch (ClassCastException e) {
        error = e;
        // fallthrough
      } catch (IllegalAccessException e) {
        error = e;
        // fallthrough
      } catch (IllegalArgumentException e) {
        error = e;
        // fallthrough
      } catch (InvocationTargetException e) {
        error = e;
        // fallthrough
      }
      throw new UnsupportedOperationException(Holder.FAILURE_MESSAGE, error);
    }
    throw new UnsupportedOperationException(Holder.FAILURE_MESSAGE);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isThreadCpuTimeSupported() {
    if (Holder.isThreadCpuTimeSupportedMethod != null) {
      try {
        // [unboxing.of.nullable] FALSE_POSITIVE
        // isThreadCpuTimeSupportedMethod = java.lang.management.ThreadMXBean: isThreadCpuTimeSupported
        // which always returns a non-null primitive type boolean
        return (Boolean) Holder.isThreadCpuTimeSupportedMethod.invoke(threadMxBean);
      } catch (ClassCastException e) {
        // fallthrough
      } catch (IllegalAccessException e) {
        // fallthrough
      } catch (IllegalArgumentException e) {
        // fallthrough
      } catch (InvocationTargetException e) {
        // fallthrough
      }
    }
    return false;
  }

}

