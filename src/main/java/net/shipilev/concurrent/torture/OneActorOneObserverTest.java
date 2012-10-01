package net.shipilev.concurrent.torture;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class OneActorOneObserverTest<S> {

    private static final int LOOPS = 10000;

    /**
     * Create new object to work on.
     *
     * Conventions:
     *   - this method is called only within injector thread
     *   - this method should return new object at every call, no caching
     *
     * @return fresh specimen
     */
    protected abstract S createNew();

    /**
     * Body for actor 1.
     *
     * Conventions:
     *   - this method is called only by actor 1, only once per specimen
     *   - the order vs. other actor is unspecified
     *
     * @param specimen specimen to work on
     */
    protected abstract void actor1(S specimen);

    /**
     * Body for the observer.
     *
     * Conventions:
     *   - this method is called only by arbiter thread, once per specimen
     *   - for any given specimen, observer would be concurrently with the actor
     *   - observer can store the arbitrated state in the result array
     *   - observer can not store the reference to result array
     *
     * @param specimen specimen to work on
     * @param result result array
     * @see #resultSize()
     */
    protected abstract void observe(S specimen, byte[] result);

    /**
     * Expected result size.
     * @return result size.
     *
     * @see #observe(Object, byte[])
     */
    protected abstract int resultSize();

    /**
     * Analyze the result.
     *
     * @param result result to be analyzed
     * @return graded outcome
     */
    protected abstract Outcome test(byte[] result);


    volatile S current;

    public void run() throws InterruptedException, ExecutionException {
        System.out.println("Running " + this.getClass().getName());

        ExecutorService pool = Executors.newFixedThreadPool(3);

        current = createNew();

        pool.submit(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    current = createNew();
                }
            }
        });

        pool.submit(new Runnable() {
            public void run() {
                S last = null;

                while (!Thread.interrupted()) {
                    int c = 0;
                    int l = 0;
                    while (l < LOOPS) {
                        S cur = current;
                        if (last != cur) {
                            actor1(cur);
                            last = cur;
                            c++;
                        }
                        l++;
                    }
                }
            }
        });

        Future<Multiset<Long>> res = pool.submit(new Callable<Multiset<Long>>() {
            public Multiset<Long> call() {
                S last = null;

                byte[] res = new byte[8];

                Multiset<Long> set = TreeMultiset.create();

                byte[][] results = new byte[LOOPS][];
                while (!Thread.interrupted()) {
                    int c = 0;
                    int l = 0;
                    while (l < LOOPS) {
                        S cur = current;
                        if (last != cur) {
                            observe(cur, res);
                            results[c] = Arrays.copyOf(res, 8);
                            last = cur;
                            c++;
                        }
                        l++;
                    }

                    for (int i = 0; i < c; i++) {
                        set.add(byteArrToLong(results[i]));
                    }
                }
                return set;
            }
        });

        TimeUnit.SECONDS.sleep(5);

        pool.shutdownNow();

        Multiset<Long> results = res.get();
        for (Multiset.Entry<Long> e : results.entrySet()) {

            byte[] b = longToByteArr(e.getElement());

            b = Arrays.copyOf(b, resultSize());

            boolean isFailed;
            switch (test(b)) {
                case ACCEPTABLE:
                case TRANSIENT:
                    // no implementation yet
                    isFailed = false;
                    break;

                case EXPECTED:
                    isFailed = (e.getCount() == 0);
                    break;

                case NOT_EXPECTED:
                    isFailed = (e.getCount() > 0);
                    break;
                default:
                    throw new IllegalStateException();
            }

            System.out.printf("%20s (%10d) %20s\n", Arrays.toString(b), e.getCount(), isFailed ? "ERROR: " + test(b) : "");
        }

        pool.awaitTermination(1, TimeUnit.DAYS);
    }

    private byte[] longToByteArr(Long element) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(element);
        return buf.array();
    }

    public static long byteArrToLong(byte[] b) {
        ByteBuffer buf = ByteBuffer.wrap(b);
        return buf.getLong();
    }

}
