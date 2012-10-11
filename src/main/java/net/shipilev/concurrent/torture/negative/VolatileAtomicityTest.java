package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.TwoActorsOneArbiterTest;

/**
 * Tests the atomicity of volatile increment.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class VolatileAtomicityTest implements TwoActorsOneArbiterTest<VolatileAtomicityTest.Specimen> {

    public static class Specimen {
        volatile int x;
    }

    @Override
    public void actor1(Specimen s) {
        s.x++;
    }

    @Override
    public void actor2(Specimen s) {
        s.x++;
    }

    @Override
    public void arbitrate(Specimen s, byte[] result) {
        result[0] = (byte) s.x;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public int resultSize() {
        return 1;
    }

}
