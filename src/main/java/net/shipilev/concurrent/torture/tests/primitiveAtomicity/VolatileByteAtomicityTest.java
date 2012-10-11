package net.shipilev.concurrent.torture.tests.primitiveAtomicity;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

/**
 * Tests if primitive bytes experience non-atomic reads/writes.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class VolatileByteAtomicityTest implements OneActorOneObserverTest<VolatileByteAtomicityTest.Specimen> {

    public static class Specimen {
        volatile byte x;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen s) {
        s.x = (byte)0xFF;
    }

    @Override
    public void observe(Specimen s, byte[] result) {
        result[0] = s.x;
    }

    @Override
    public int resultSize() {
        return 1;
    }

}
