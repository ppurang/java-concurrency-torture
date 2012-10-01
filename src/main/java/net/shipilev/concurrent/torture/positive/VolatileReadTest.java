package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.Outcome;

public class VolatileReadTest extends OneActorOneObserverTest<VolatileReadTest.Specimen> {

    public static class Specimen {
        int x;
        volatile int y;
    }

    @Override
    public void actor1(Specimen s) {
        s.x = 1;
        s.y = 1;
    }

    @Override
    public void observe(Specimen s, byte[] res) {
        res[0] = (byte) s.x;
        res[1] = (byte) s.y;
        res[2] = (byte) s.x;
    }

    @Override
    public Specimen createNew() {
        return new Specimen();
    }

    @Override
    protected Outcome test(byte[] res) {
        if (res[1] == 1 && res[2] == 0) {
            // volatile read had happened
            // should always read x == 1
            return Outcome.NOT_EXPECTED;
        }
        return Outcome.ACCEPTABLE;
    }

    public int resultSize() {
        return 3;
    }

}
