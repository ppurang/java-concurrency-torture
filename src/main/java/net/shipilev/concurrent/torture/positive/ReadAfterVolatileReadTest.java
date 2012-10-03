package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.Runner;
import net.shipilev.concurrent.torture.Outcome;

/**
 * Tests if volatile write-read induce proper happens-before.
 *
 * Possible states are:
 *   [0, *]: CORRECT:   volatile write to $y had not yet happened, no constraints on $x
 *   [1, 0]: INCORRECT: can't read default value for $x after volatile write to $y happened
 *   [1, 1]: INCORRECT: can't read stale value for $x after volatile write to $y happened
 *   [1, 2]: CORRECT:   read correct $x value, forced by happens-before
 *   [1, 3]: CORRECT:   read correct $x value, residual after $y update
 */
public class ReadAfterVolatileReadTest implements OneActorOneObserverTest<ReadAfterVolatileReadTest.Specimen> {

    public static class Specimen {
        int x;
        volatile int y;
    }

    @Override
    public void actor1(Specimen s) {
        s.x = 1;
        s.x = 2;
        s.y = 1;
        s.x = 3;
    }

    @Override
    public void observe(Specimen s, byte[] res) {
        res[0] = (byte) s.y;
        res[1] = (byte) s.x;
    }

    @Override
    public Specimen newSpecimen() {
        return new Specimen();
    }

    @Override
    public Outcome test(byte[] res) {
        if (res[0] == 1 && res[1] < 2) {
            return Outcome.NOT_EXPECTED;
        }
        return Outcome.ACCEPTABLE;
    }

    public int resultSize() {
        return 2;
    }

}
