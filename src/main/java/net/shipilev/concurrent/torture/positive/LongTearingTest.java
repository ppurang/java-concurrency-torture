package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.Outcome;
import net.shipilev.concurrent.torture.TwoThreadTest;

public class LongTearingTest extends TwoThreadTest<LongTearingTest.Specimen> {

    public static class Specimen {
        long x;
    }

    @Override
    public void thread0(Specimen s) {
        s.x = 0xFFFFFFFFFFFFFFFFL;
    }

    @Override
    public void thread1(Specimen s, byte[] res) {
        long t = s.x;
        res[0] = (byte) ((t >> 0) & 0xFFFF);
        res[1] = (byte) ((t >> 8) & 0xFFFF);
        res[2] = (byte) ((t >> 16) & 0xFFFF);
        res[3] = (byte) ((t >> 24) & 0xFFFF);
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
        return 4;
    }

}
