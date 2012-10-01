package net.shipilev.concurrent.torture;

import net.shipilev.concurrent.torture.util.Multiset;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This test accepts two actors (threads actively mutating the state),
 * and one arbiter (thread observing the state *after* two actors finished).
 *
 * @param <S> specimen type
 */
public abstract class TwoActorsOneArbiterTest<S> {

    /**
     * Create new object to work on.
     *
     * Conventions:
     *   - this method is called only within injector thread
     *   - this method should return new object at every call, no caching
     *
     * @return fresh specimen
     */
    protected abstract S newSpecimen();

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
     * Body for actor 2.
     *
     * Conventions:
     *   - this method is called only by actor 2, only once per specimen
     *   - the order vs. other actor is unspecified
     *
     * @param specimen specimen to work on
     */
    protected abstract void actor2(S specimen);

    /**
     * Body for the arbiter.
     *
     * Conventions:
     *   - this method is called only by arbiter thread, once per specimen
     *   - for any given specimen, arbiter would be called *after* both actors finished with the specimen
     *   - all memory effects on specimen would make the effect before arbitrate() call
     *      (i.e. for given specimen, (actor1() hb arbitrate()) and (actor2() hb arbitrate()))
     *   - arbiter can store the arbitrated state in the result array
     *   - arbiter can not store the reference to result array
     *
     * @param specimen specimen to work on
     * @param result result array
     * @see #resultSize()
     */
    protected abstract void arbitrate(S specimen, byte[] result);

    /**
     * Expected result size.
     * @return result size.
     *
     * @see #arbitrate(Object, byte[])
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
    volatile S t1;
    volatile S t2;

    public void run() throws InterruptedException, ExecutionException {
        System.out.println("Running " + this.getClass().getName());

        current = newSpecimen();

        ExecutorService pool = Executors.newCachedThreadPool();

        pool.submit(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    while (t1 != null && t2 != null && !Thread.currentThread().isInterrupted());
                    current = newSpecimen();
                }
            }
        });

        pool.submit(new Runnable() {
            public void run() {
                S last = null;
                while (!Thread.interrupted()) {
                    int c = 0;
                    int l = 0;
                    while (l < Constants.LOOPS) {
                        S cur = current;
                        if (last != cur) {
                            actor1(cur);
                            t1 = cur;
                            last = cur;
                            c++;
                        }
                        l++;
                    }
                }
            }
        });

        pool.submit(new Runnable() {
            public void run() {
                S last = null;
                while (!Thread.interrupted()) {
                    int c = 0;
                    int l = 0;
                    while (l < Constants.LOOPS) {
                        S cur = current;
                        if (last != cur) {
                            actor2(cur);
                            last = cur;
                            t2 = cur;
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

                Multiset<Long> set = new Multiset<Long>();

                byte[][] results = new byte[Constants.LOOPS][];
                while (!Thread.interrupted()) {
                    int c = 0;
                    int l = 0;
                    while (l < Constants.LOOPS) {
                        S s1 = t1;
                        S s2 = t2;
                        if (s1 == s2 && s1 != null) {
                            arbitrate(s1, res);
                            results[c] = new byte[8];
                            System.arraycopy(res, 0, results[c], 0, 8);
                            c++;
                            t1 = null;
                            t2 = null;
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

        TimeUnit.MILLISECONDS.sleep(Constants.TIME_MSEC);

        pool.shutdownNow();

        Multiset<Long> results = res.get();
        for (Long e : results.keys()) {

            byte[] b = longToByteArr(e);

            byte[] t = new byte[resultSize()];
            System.arraycopy(b, 0, t, 0, resultSize());
            b = t;

            boolean isFailed;
            switch (test(b)) {
                case ACCEPTABLE:
                case TRANSIENT:
                    // no implementation yet
                    isFailed = false;
                    break;

                case EXPECTED:
                    isFailed = (results.count(e) == 0);
                    break;

                case NOT_EXPECTED:
                    isFailed = (results.count(e) > 0);
                    break;
                default:
                    throw new IllegalStateException();
            }

            System.out.printf("%35s (%10d) %20s\n", Arrays.toString(b), results.count(e), isFailed ? "ERROR: " + test(b) : "");
        }

        pool.awaitTermination(3600, TimeUnit.SECONDS);
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
