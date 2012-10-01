package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.Outcome;

public class IntTearingTest extends OneActorOneObserverTest<IntTearingTest.Specimen> {

    public static class Specimen {
        int x;
    }

    @Override
    public Specimen createNew() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen s) {
        s.x = 0xFFFFFFFF;
    }

    @Override
    protected void observe(Specimen s, byte[] result) {
        long t = s.x;
        result[0] = (byte) ((t >> 0) & 0xFFFF);
        result[1] = (byte) ((t >> 8) & 0xFFFF);
        result[2] = (byte) ((t >> 16) & 0xFFFF);
        result[3] = (byte) ((t >> 24) & 0xFFFF);
    }

    @Override
    protected Outcome test(byte[] res) {
        if (res[0] != res[1] || res[1] != res[2] || res[2] != res[3])
            return Outcome.NOT_EXPECTED;
        return Outcome.ACCEPTABLE;
    }

    public int resultSize() {
        return 4;
    }

}
