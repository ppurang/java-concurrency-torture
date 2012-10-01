package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.Outcome;
import net.shipilev.concurrent.torture.ThreeThreadTest;
import net.shipilev.concurrent.torture.TwoThreadTest;

import java.util.concurrent.atomic.AtomicInteger;

public class VolatileAtomicityTest extends ThreeThreadTest<VolatileAtomicityTest.Specimen> {

    public static class Specimen {
        volatile int x;
        AtomicInteger count = new AtomicInteger();
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
        return Outcome.ACCEPTABLE;
    }

    public int resultSize() {
        return 2;
    }

}
