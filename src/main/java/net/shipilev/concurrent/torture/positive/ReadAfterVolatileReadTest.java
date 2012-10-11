package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;

/**
 * Tests if volatile write-read induce proper happens-before.
 *
 *  @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
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
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public int resultSize() {
        return 2;
    }

}
