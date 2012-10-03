package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.Outcome;

/**
 * Tests if primitive doubles experience non-atomic updates.
 * Long tearing is allowed by JMM, and hence this is a negative test.
 * The failure on this test DOES NOT highlight the possible bug.
 *
 * Possible observed states:
 *    - default value for default (i.e. 0)
 *    - value set by actor (i.e. -1)
 *    - low-word set to -1, high-word still set to 0 (tearing)
 *    - high-word set to -1, low-word still set to 0 (tearing)
 *
 * All other values are forbidden because out-of-thin-air values are forbidden.
 */
public class DoubleAtomicityTest implements OneActorOneObserverTest<DoubleAtomicityTest.Specimen> {

    public static class Specimen {
        double x;
    }

    @Override
    public Specimen newSpecimen() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen s) {
        s.x = Double.longBitsToDouble(0xFFFFFFFFFFFFFFFFL);
    }

    @Override
    public void observe(Specimen s, byte[] result) {
        long t = Double.doubleToRawLongBits(s.x);
        result[0] = (byte) ((t >> 0) & 0xFF);
        result[1] = (byte) ((t >> 8) & 0xFF);
        result[2] = (byte) ((t >> 16) & 0xFF);
        result[3] = (byte) ((t >> 24) & 0xFF);
        result[4] = (byte) ((t >> 32) & 0xFF);
        result[5] = (byte) ((t >> 40) & 0xFF);
        result[6] = (byte) ((t >> 48) & 0xFF);
        result[7] = (byte) ((t >> 56) & 0xFF);
    }

    @Override
    public Outcome test(byte[] res) {
        if (res[0] != res[1] || res[1] != res[2] || res[2] != res[3]
                || res[3] != res[4] || res[5] != res[6] || res[6] != res[7])
            return Outcome.NOT_EXPECTED;

        return Outcome.ACCEPTABLE;
    }

    public int resultSize() {
        return 8;
    }

}