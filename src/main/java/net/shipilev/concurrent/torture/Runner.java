package net.shipilev.concurrent.torture;

import net.shipilev.concurrent.torture.util.Multiset;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Basic runner for concurrency tests.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class Runner {
    private final PrintWriter pw;
    private final PrintWriter xml;
    private final int time;
    private final int loops;

    public Runner(Options opts) throws FileNotFoundException {
        this.pw = new PrintWriter(System.out, true);
        this.xml = new PrintWriter(opts.getResultFile());
        time = opts.getTime();
        loops = opts.getLoops();
    }

    public ExecutorService getPool(int threads) {
        if (Runtime.getRuntime().availableProcessors() < threads) {
            pw.println("WARNING: This test should be run with at least " + threads + " CPUs to get reliable results");
        }
        return Executors.newCachedThreadPool();
    }

    /**
     * Run the test.
     * This method blocks until test is complete
     *
     * @param test test to run
     * @param <S> test state object type
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public <S> void run(final OneActorOneObserverTest<S> test) throws InterruptedException, ExecutionException {
        pw.println("Running " + test.getClass().getName());

        ExecutorService pool = getPool(3);

        final SingleSharedStateHolder<S> holder = new SingleSharedStateHolder<S>();

        // current should be null so that injector could inject the first instance
        holder.current = null;

        /*
           Injector thread: injects new states until interrupted.
         */
        pool.submit(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    S[] newStride = (S[]) new Object[loops];
                    for (int c = 0; c < loops; c++) {
                        newStride[c] = test.newState();
                    }

                    while (holder.current != null) {
                        if (Thread.interrupted()) {
                            return;
                        }
                    }
                    holder.current = newStride;
                }
            }
        });

        /*
           Actor 1 thread.
           The rationale for its loop is as follows:
              a. We should be easy on checking the interrupted status, hence we do $LOOPS internally
              b. Thread should not observe the state object more than once
         */
        pool.submit(new Runnable() {
            public void run() {
                S[] last = null;

                while (!Thread.interrupted()) {
                    S[] cur = holder.current;
                    if (cur != null && last != cur) {
                        for (int l = 0; l < loops; l++) {
                            test.actor1(cur[l]);
                        }
                        last = cur;
                    }
                }
            }
        });

        /*
          Observer thread.
          The rationale for its loop is as follows:
              a. We should be easy on checking the interrupted status, hence we do $LOOPS internally
              b. Thread should not observe the state object more than once
              c. The overhead of doing the work inside the inner loop should be small
              d. $state is getting reused, so we end up marshalling it to long to count properly
        */
        Future<Multiset<Long>> res = pool.submit(new Callable<Multiset<Long>>() {
            public Multiset<Long> call() {
                Multiset<Long> set = new Multiset<Long>();

                S[] last = null;
                byte[] state = new byte[8];
                byte[][] results = new byte[loops][];

                while (!Thread.interrupted()) {
                    S[] cur = holder.current;

                    if (cur != null && last != cur) {
                        for (int l = 0; l < loops; l++) {
                            test.observe(cur[l], state);
                            results[l] = new byte[8];
                            System.arraycopy(state, 0, results[l], 0, 8);
                        }

                        last = cur;

                        for (int i = 0; i < loops; i++) {
                            set.add(byteArrToLong(results[i]));
                        }

                        // let others proceed
                        holder.current = null;
                    }
                }
                return set;
            }
        });

        TimeUnit.MILLISECONDS.sleep(time);

        pool.shutdownNow();
        pool.awaitTermination(3600, TimeUnit.SECONDS);

        dump(test, res.get());
        judge(test, res.get());
    }

    public <S> void run(final TwoActorsOneArbiterTest<S> test) throws InterruptedException, ExecutionException {
        pw.println("Running " + test.getClass().getName());

        ExecutorService pool = getPool(4);

        final TwoSharedStateHolder<S> holder = new TwoSharedStateHolder<S>();

        // need to initialize so that actor thread will not NPE.
        // once injector catches up, it will push fresh state objects
        holder.current = test.newState();

         /*
           Injector thread: injects new states until interrupted.
           There are an additional constraints:
              a. If actors results are not yet consumed, do not push the new state.
                 This will effectively block actors from working until arbiter consumes their result.
         */
        pool.submit(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    while (holder.t1 != null && holder.t2 != null && !Thread.currentThread().isInterrupted());
                    holder.current = test.newState();
                }
            }
        });

        /*
           Actor 1 thread.
           The rationale for its loop is as follows:
              a. We should be easy on checking the interrupted status, hence we do $LOOPS internally
              b. Thread should not observe the state object more than once
              c. Once thread is done with its work, it publishes the reference to state object for arbiter
         */
        pool.submit(new Runnable() {
            public void run() {
                S last = null;
                while (!Thread.interrupted()) {
                    int l = 0;
                    while (l < loops) {
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

        /*
           Actor 2 thread.
           The rationale for its loop is as follows:
              a. We should be easy on checking the interrupted status, hence we do $LOOPS internally
              b. Thread should not observe the state object more than once
              c. Once thread is done with its work, it publishes the reference to state object for arbiter
         */
        pool.submit(new Runnable() {
            public void run() {
                S last = null;
                while (!Thread.interrupted()) {
                    int l = 0;
                    while (l < loops) {
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

        /*
          Arbiter thread.
          The rationale for its loop is as follows:
              a. We should be easy on checking the interrupted status, hence we do $LOOPS internally
              b. Thread should not observe the state object more than once
              c. The overhead of doing the work inside the inner loop should be small
              d. $state is getting reused, so we end up marshalling it to long to count properly
              e. Arbiter waits until both actors have finished their work and published their results
        */
        Future<Multiset<Long>> res = pool.submit(new Callable<Multiset<Long>>() {
            public Multiset<Long> call() {
                byte[] res = new byte[8];

                Multiset<Long> set = new Multiset<Long>();

                byte[][] results = new byte[loops][];
                while (!Thread.interrupted()) {
                    int c = 0;
                    int l = 0;
                    while (l < loops) {
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

        TimeUnit.MILLISECONDS.sleep(time);

        pool.shutdownNow();
        pool.awaitTermination(3600, TimeUnit.SECONDS);

        dump(test, res.get());
        judge(test, res.get());
    }

    private void dump(Evaluator evaluator, Multiset<Long> results) {
        xml.println("<result>");
        xml.println("<test>" + evaluator.getClass().getName() + "</test>");
        xml.println("<states>");
        for (Long e : results.keys()) {
            byte[] b = longToByteArr(e);
            byte[] t = new byte[evaluator.resultSize()];
            System.arraycopy(b, 0, t, 0, evaluator.resultSize());
            b = t;

            xml.println("<state>");
            xml.println("<id>" + Arrays.toString(b) + "</id>");
            xml.println("<count>" + results.count(e) + "</count>");
            xml.println("</state>");
        }
        xml.println("</states>");
        xml.println("</result>");
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

    public void close() {
        xml.close();
    }

    public static class SingleSharedStateHolder<S> {
        volatile S[] current;
    }

    public static class TwoSharedStateHolder<S> {
        volatile S current;
        volatile S t1;
        volatile S t2;
    }

}
