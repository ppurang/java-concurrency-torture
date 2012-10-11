package net.shipilev.concurrent.torture.tests.primitiveAtomicity;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

/**
 * Tests if primitive shorts experience non-atomic reads/writes.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class VolatileShortAtomicityTest implements OneActorOneObserverTest<VolatileShortAtomicityTest.Specimen> {

    public static class Specimen {
        volatile short x;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen s) {
        s.x = (short)0xFFFF;
    }

    @Override
    public void observe(Specimen s, byte[] result) {
        short t = s.x;
        result[0] = (byte) ((t >> 0) & 0xFF);
        result[1] = (byte) ((t >> 8) & 0xFF);
    }

    @Override
    public int resultSize() {
        return 2;
    }

}
