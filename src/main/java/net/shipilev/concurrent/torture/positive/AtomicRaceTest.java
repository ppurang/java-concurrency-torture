package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.Outcome;
import net.shipilev.concurrent.torture.TwoActorsOneArbiterTest;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicRaceTest extends TwoActorsOneArbiterTest<AtomicRaceTest.Specimen> {

    public static class Specimen {
        final AtomicInteger count = new AtomicInteger();
    }

    @Override
    public void actor1(Specimen s) {
        s.count.incrementAndGet();
    }

    @Override
    public void actor2(Specimen s) {
        s.count.incrementAndGet();
    }

    @Override
    public void arbitrate(Specimen s, byte[] result) {
        result[0] = (byte) s.count.get();
    }

    @Override
    public Specimen createNew() {
        return new Specimen();
    }

    @Override
    protected Outcome test(byte[] result) {
        if (result[0] != 2)
            return Outcome.NOT_EXPECTED;
        return Outcome.ACCEPTABLE;
    }

    public int resultSize() {
        return 1;
    }

}
