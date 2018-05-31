package org.junit.internal.management;

import org.junit.internal.Classes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link RuntimeMXBean} using the JVM reflectively.
 */
final class ReflectiveRuntimeMXBean implements RuntimeMXBean {
  private final Object runtimeMxBean;

  @SuppressWarnings("nullness")
  private static final class Holder {
    private static final Method getInputArgumentsMethod;
    static {
      Method inputArguments = null;
      try {
        Class<?> threadMXBeanClass = Classes.getClass("java.lang.management.RuntimeMXBean");
        inputArguments = threadMXBeanClass.getMethod("getInputArguments");
      } catch (ClassNotFoundException e) {
        // do nothing, input arguments will be null on failure
      } catch (NoSuchMethodException e) {
        // do nothing, input arguments will be null on failure
      } catch (SecurityException e) {
        // do nothing, input arguments will be null on failure
      }
      // [assignment.type.incompatible] FALSE_POSITIVE
      // java.lang.management.RuntimeMXBean class exist and
      // has getInputArguments method
      // so getInputArgumentsMethod cannot be null
      getInputArgumentsMethod = inputArguments;
    }
  }

  ReflectiveRuntimeMXBean(Object runtimeMxBean) {
    super();
    this.runtimeMxBean = runtimeMxBean;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({"unchecked","nullness"})
  public List<String> getInputArguments() {
    if (Holder.getInputArgumentsMethod != null) {
      try {
        // [return.type.incompatible] FALSE_POSITIVE
        // getInputArgumentsMethod always exists and
        // return a non-null list by its documentation
        // @see java.lang.management.RuntimeMXBean#getInputArguments()
        return (List<String>) Holder.getInputArgumentsMethod.invoke(runtimeMxBean);
      } catch (ClassCastException e) { // no multi-catch with source level 6
        // fallthrough
      } catch (IllegalAccessException e) {
        // fallthrough
      } catch (IllegalArgumentException e) {
        // fallthrough
      } catch (InvocationTargetException e) {
        // fallthrough
      }
    }
    return Collections.emptyList();
  }

}

