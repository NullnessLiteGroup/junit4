package org.junit.runner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A <code>Result</code> collects and summarizes information from running multiple tests.
 * All tests are counted -- additional information is collected from tests that fail.
 *
 * @since 4.0
 */
public class Result implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("nullness")
    // [dereference.of.nullable] FALSE_POSITIVE
    //  de-referencing lookup() cannot raise NPE in this case,
    // since SerializedFrom.class implements Serializable so that lookup() will not return null
    private static final ObjectStreamField[] serialPersistentFields =
            ObjectStreamClass.lookup(SerializedForm.class).getFields();
    private final @Nullable AtomicInteger count;
    private final @Nullable AtomicInteger ignoreCount;
    private final CopyOnWriteArrayList<Failure> failures;
    private final AtomicLong runTime;
    private final AtomicLong startTime;

    /** Only set during deserialization process. */
    private SerializedForm serializedForm;

    public Result() {
        count = new AtomicInteger();
        ignoreCount = new AtomicInteger();
        failures = new CopyOnWriteArrayList<Failure>();
        runTime = new AtomicLong();
        startTime = new AtomicLong();
    }

    private Result(SerializedForm serializedForm) {
        count = serializedForm.fCount;
        ignoreCount = serializedForm.fIgnoreCount;
        failures = new CopyOnWriteArrayList<Failure>(serializedForm.fFailures);
        runTime = new AtomicLong(serializedForm.fRunTime);
        startTime = new AtomicLong(serializedForm.fStartTime);
    }

    /**
     * @return the number of tests run
     */
    @SuppressWarnings("nullness")
    public int getRunCount() {
        // [dereference.of.nullable] FALSE_POSITIVE
        //  count.get() cannot raise NPE
        // count can only be null if the private constructor is called,
        // and the private constructor is only called from another private method,
        // readResolve,which is called nowhere in this scope.
        // Besides, count is final.
        return count.get();
    }

    /**
     * @return the number of tests that failed during the run
     */
    public int getFailureCount() {
        return failures.size();
    }

    /**
     * @return the number of milliseconds it took to run the entire suite to run
     */
    public long getRunTime() {
        return runTime.get();
    }

    /**
     * @return the {@link Failure}s describing tests that failed and the problems they encountered
     */
    public List<Failure> getFailures() {
        return failures;
    }

    /**
     * @return the number of tests ignored during the run
     */
    @SuppressWarnings("nullness")
    public int getIgnoreCount() {
        // [dereference.of.nullable] FALSE_POSITIVE
        //  ignoreCount.get() cannot raise NPE
        // ignoreCount can only be null if the private constructor is called,
        // and the private constructor is only called from another private method,
        // readResolve,which is called nowhere in this scope.
        // Besides, ignoreCount is final.
        return ignoreCount.get();
    }

    /**
     * @return <code>true</code> if all tests succeeded
     */
    public boolean wasSuccessful() {
        return getFailureCount() == 0;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        SerializedForm serializedForm = new SerializedForm(this);
        serializedForm.serialize(s);
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        serializedForm = SerializedForm.deserialize(s);
    }

    private Object readResolve()  {
        return new Result(serializedForm);
    }

    @RunListener.ThreadSafe
    private class Listener extends RunListener {
        @Override
        public void testRunStarted(Description description) throws Exception {
            startTime.set(System.currentTimeMillis());
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            long endTime = System.currentTimeMillis();
            runTime.addAndGet(endTime - startTime.get());
        }

        @Override
        @SuppressWarnings("nullness")
        public void testFinished(Description description) throws Exception {
            // [dereference.of.nullable] FALSE_POSITIVE
            //  de-referencing count cannot raise NPE
            // count can only be null if the private constructor is called,
            // and the private constructor is only called from another private method,
            // readResolve,which is called nowhere in this scope.
            // Besides, count is final.
            count.getAndIncrement();
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            failures.add(failure);
        }

        @Override
        @SuppressWarnings("nullness")
        public void testIgnored(Description description) throws Exception {
            // [dereference.of.nullable] FALSE_POSITIVE
            //  de-referencing ignoreCount cannot raise NPE
            // ignoreCount can only be null if the private constructor is called,
            // and the private constructor is only called from another private method,
            // readResolve,which is called nowhere in this scope.
            // Besides, ignoreCount is final.
            ignoreCount.getAndIncrement();
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            // do nothing: same as passing (for 4.5; may change in 4.6)
        }
    }

    /**
     * Internal use only.
     */
    public RunListener createListener() {
        return new Listener();
    }

    /**
     * Represents the serialized output of {@code Result}. The fields on this
     * class match the files that {@code Result} had in JUnit 4.11.
     */
    private static class SerializedForm implements Serializable {
        private static final long serialVersionUID = 1L;
        private final @Nullable AtomicInteger fCount;
        private final @Nullable AtomicInteger fIgnoreCount;
        private final @Nullable List<Failure> fFailures;
        private final long fRunTime;
        private final long fStartTime;

        public SerializedForm(Result result) {
            fCount = result.count;
            fIgnoreCount = result.ignoreCount;
            fFailures = Collections.synchronizedList(new ArrayList<Failure>(result.failures));
            fRunTime = result.runTime.longValue();
            fStartTime = result.startTime.longValue();
        }

        @SuppressWarnings("unchecked")
        private SerializedForm(ObjectInputStream.GetField fields) throws IOException {
            fCount = (AtomicInteger) fields.get("fCount", null);
            fIgnoreCount = (AtomicInteger) fields.get("fIgnoreCount", null);
            fFailures = (List<Failure>) fields.get("fFailures", null);
            fRunTime = fields.get("fRunTime", 0L);
            fStartTime = fields.get("fStartTime", 0L);
        }

        public void serialize(ObjectOutputStream s) throws IOException {
            ObjectOutputStream.PutField fields = s.putFields();
            fields.put("fCount", fCount);
            fields.put("fIgnoreCount", fIgnoreCount);
            fields.put("fFailures", fFailures);
            fields.put("fRunTime", fRunTime);
            fields.put("fStartTime", fStartTime);
            s.writeFields();
        }

        public static SerializedForm deserialize(ObjectInputStream s)
                throws ClassNotFoundException, IOException {
            ObjectInputStream.GetField fields = s.readFields();
            return new SerializedForm(fields);
        }
    }
}
