package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.Outcome;
import net.shipilev.concurrent.torture.ThreeThreadTest;

import java.util.concurrent.atomic.AtomicInteger;

public class TwoAtomicsAreNotAtomicTest extends ThreeThreadTest<TwoAtomicsAreNotAtomicTest.Specimen> {

    public static class Specimen {
        final AtomicInteger count1 = new AtomicInteger();
        final AtomicInteger count2 = new AtomicInteger();
    }

    @Override
    public void thread0(Specimen s) {
        s.count1.incrementAndGet();
        s.count2.incrementAndGet();
    }

    @Override
    public void thread1(Specimen s) {
        s.count1.incrementAndGet();
        s.count2.incrementAndGet();
    }

    @Override
    public void thread2(Specimen s, byte[] res) {
        // can read different values due to pending race against thread0 and thread1
        res[0] = (byte) s.count1.get();
        res[1] = (byte) s.count2.get();
    }

    @Override
    public Specimen createNew() {
        return new Specimen();
    }

    @Override
    protected Outcome test(byte[] res) {
        if (res[0] != res[1]) return Outcome.NOT_EXPECTED;
        return Outcome.ACCEPTABLE;
    }

    public int resultSize() {
        return 2;
    }

}
