package net.shipilev.concurrent.torture;

import net.shipilev.concurrent.torture.util.Multiset;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Runner {
    private final PrintWriter pw;

    public Runner() {
        this.pw = new PrintWriter(System.out, true);
    }

    public static class SingleSharedStateHolder<S> {
        volatile S current;
    }

    public ExecutorService getPool(int threads) {
        if (Runtime.getRuntime().availableProcessors() < threads) {
            pw.println("WARNING: This test should be run with at least " + threads + " CPUs to get reliable results");
        }
        return Executors.newFixedThreadPool(threads);
    }

    public <S> void run(final OneActorOneObserverTest<S> test) throws InterruptedException, ExecutionException {
        pw.println("Running " + test.getClass().getName());

        ExecutorService pool = getPool(3);

        final SingleSharedStateHolder<S> holder = new SingleSharedStateHolder<S>();
        holder.current = test.newState();

        pool.submit(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    holder.current = test.newState();
                }
            }
        });

        pool.submit(new Runnable() {
            public void run() {
                S last = null;

                while (!Thread.interrupted()) {
                    int l = 0;
                    while (l < Constants.LOOPS) {
                        S cur = holder.current;
                        if (last != cur) {
                            test.actor1(cur);
                            last = cur;
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
                        S cur = holder.current;
                        if (last != cur) {
                            test.observe(cur, res);
                            results[c] = new byte[8];
                            System.arraycopy(res, 0, results[c], 0, 8);
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

        TimeUnit.MILLISECONDS.sleep(Constants.TIME_MSEC);

        pool.shutdownNow();
        pool.awaitTermination(3600, TimeUnit.SECONDS);

        judge(test, res.get());
    }


    public static class TwoSharedStateHolder<S> {
        volatile S current;
        volatile S t1;
        volatile S t2;
    }

    public <S> void run(final TwoActorsOneArbiterTest<S> test) throws InterruptedException, ExecutionException {
        System.out.println("Running " + test.getClass().getName());

        ExecutorService pool = getPool(4);

        final TwoSharedStateHolder<S> holder = new TwoSharedStateHolder<S>();
        holder.current = test.newState();

        pool.submit(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    while (holder.t1 != null && holder.t2 != null && !Thread.currentThread().isInterrupted());
                    holder.current = test.newState();
                }
            }
        });

        pool.submit(new Runnable() {
            public void run() {
                S last = null;
                while (!Thread.interrupted()) {
                    int l = 0;
                    while (l < Constants.LOOPS) {
                        S cur = holder.current;
                        if (last != cur) {
                            test.actor1(cur);
                            holder.t1 = cur;
                            last = cur;
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
                    int l = 0;
                    while (l < Constants.LOOPS) {
                        S cur = holder.current;
                        if (last != cur) {
                            test.actor2(cur);
                            last = cur;
                            holder.t2 = cur;
                        }
                        l++;
                    }
                }
            }
        });

        Future<Multiset<Long>> res = pool.submit(new Callable<Multiset<Long>>() {
            public Multiset<Long> call() {
                byte[] res = new byte[8];

                Multiset<Long> set = new Multiset<Long>();

                byte[][] results = new byte[Constants.LOOPS][];
                while (!Thread.interrupted()) {
                    int c = 0;
                    int l = 0;
                    while (l < Constants.LOOPS) {
                        S s1 = holder.t1;
                        S s2 = holder.t2;
                        if (s1 == s2 && s1 != null) {
                            test.arbitrate(s1, res);
                            results[c] = new byte[8];
                            System.arraycopy(res, 0, results[c], 0, 8);
                            c++;
                            holder.t1 = null;
                            holder.t2 = null;
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
        pool.awaitTermination(3600, TimeUnit.SECONDS);

        judge(test, res.get());
    }

    private void judge(Evaluator evaluator, Multiset<Long> results) {
        pw.printf("%35s %12s %-20s\n", "Observed state", "Occurrences", "Interpretation");
        for (Long e : results.keys()) {

            byte[] b = longToByteArr(e);

            byte[] t = new byte[evaluator.resultSize()];
            System.arraycopy(b, 0, t, 0, evaluator.resultSize());
            b = t;

            boolean isFailed;
            switch (evaluator.test(b)) {
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

            pw.printf("%35s (%10d) %6s %-40s\n", Arrays.toString(b), results.count(e), (isFailed ? "ERROR:" : "OK:"), evaluator.test(b));
        }
        pw.println();
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
