package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.Outcome;
import net.shipilev.concurrent.torture.ThreeThreadTest;

import java.util.concurrent.atomic.AtomicInteger;

public class VolatileAtomicityTest extends ThreeThreadTest<VolatileAtomicityTest.Specimen> {

    // FIXME: This test is incorrect, because stronger requirement in TwoAtomicsAreNotAtomicTest is not met

    public static class Specimen {
        volatile int x;
        final AtomicInteger count = new AtomicInteger();
    }

    @Override
    public void thread0(Specimen s) {
        s.x++;
        s.count.incrementAndGet();
    }

    @Override
    public void thread1(Specimen s) {
        s.x++;
        s.count.incrementAndGet();
    }

    @Override
    public void thread2(Specimen s, byte[] res) {
        res[0] = (byte) s.x;
        res[1] = (byte) s.count.get();
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
