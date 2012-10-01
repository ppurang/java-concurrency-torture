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

public abstract class ThreeThreadTest<S> {

    private static final int LOOPS = 10000;

    volatile S current;

    public void run() throws InterruptedException, ExecutionException {
        System.out.println("Running " + this.getClass().getName());

        current = createNew();

        ExecutorService pool = Executors.newCachedThreadPool();

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
                            thread0(cur);
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
                    while (l < LOOPS) {
                        S cur = current;
                        if (last != cur) {
                            thread1(cur);
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
                            thread2(cur, res);
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

        TimeUnit.SECONDS.sleep(1);

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

    protected abstract Outcome test(byte[] d);

    protected abstract int resultSize();

    public abstract S createNew();
    public abstract void thread0(S current);
    public abstract void thread1(S current);
    public abstract void thread2(S current, byte[] res);

}
