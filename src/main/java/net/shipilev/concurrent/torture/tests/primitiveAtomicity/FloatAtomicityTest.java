package net.shipilev.concurrent.torture.tests.primitiveAtomicity;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

/**
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class FloatAtomicityTest implements OneActorOneObserverTest<FloatAtomicityTest.Specimen> {

    public static class Specimen {
        float x;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen s) {
        s.x = Float.intBitsToFloat(0xFFFFFFFF);
    }

    @Override
    public void observe(Specimen s, byte[] result) {
        int t = Float.floatToRawIntBits(s.x);
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
