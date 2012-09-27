package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.Outcome;
import net.shipilev.concurrent.torture.TwoThreadTest;

public class SyncBarrierTest extends TwoThreadTest<SyncBarrierTest.Specimen> {

    public static class Specimen {
        int x;
        int y;
    }

    @Override
    public void thread0(Specimen s) {
        s.x = 42;
        s.y = 1;
    }

    @Override
    public void thread1(Specimen s, byte[] res) {
        res[0] = (byte) s.x;
        res[1] = (byte) s.y;
        synchronized (this) {} // THIS IS *NOT* A PROPER BARRIER
        res[2] = (byte) s.x;
    }

    @Override
    public Specimen createNew() {
        return new Specimen();
    }

    @Override
    protected Outcome test(byte[] res) {
        if (res[1] == 1 && res[2] == 0) {
            // "barrier" happened
            // should always read x != 0
            return Outcome.NOT_EXPECTED;
        }
        return Outcome.ACCEPTABLE;
    }

    public int resultSize() {
        return 3;
    }

}
