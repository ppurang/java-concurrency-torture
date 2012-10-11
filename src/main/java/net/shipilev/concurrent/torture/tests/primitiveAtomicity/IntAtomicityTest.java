package net.shipilev.concurrent.torture.tests.primitiveAtomicity;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;

/**
 * Tests if primitive integers experience non-atomic reads/writes.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class IntAtomicityTest implements OneActorOneObserverTest<IntAtomicityTest.Specimen> {

    public static class Specimen {
        int x;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen s) {
        s.x = 0xFFFFFFFF;
    }

    @Override
    public void observe(Specimen s, byte[] result) {
        long t = s.x;
        result[0] = (byte) ((t >> 0) & 0xFF);
        result[1] = (byte) ((t >> 8) & 0xFF);
        result[2] = (byte) ((t >> 16) & 0xFF);
        result[3] = (byte) ((t >> 24) & 0xFF);
    }

    @Override
    public int resultSize() {
        return 4;
    }

}
