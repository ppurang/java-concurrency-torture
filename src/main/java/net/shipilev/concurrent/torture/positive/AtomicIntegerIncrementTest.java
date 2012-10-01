package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.Outcome;
import net.shipilev.concurrent.torture.TwoActorsOneArbiterTest;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the atomicity of AtomicInteger incrementAndGet.
 * This is guaranteed by JMM and implementation.
 * The failure on this scenario highlights the possible bug.
 *
 * Possible observed states are:
 *    [0]: INCORRECT: complete infrastructure failure (this is an infrastructure bug to have one)
 *    [1]: INCORRECT: lost update
 *    [2]: CORRECT:   both updates are intact
 */
public class AtomicIntegerIncrementTest extends TwoActorsOneArbiterTest<AtomicIntegerIncrementTest.Specimen> {

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
