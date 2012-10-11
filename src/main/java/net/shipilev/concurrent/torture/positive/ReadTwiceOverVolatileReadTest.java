package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;

/**
 * Test if volatile write-read induces happens-before if in between two non-volatile reads.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class ReadTwiceOverVolatileReadTest implements OneActorOneObserverTest<ReadTwiceOverVolatileReadTest.Specimen> {

    public static class Specimen {
        int x;
        volatile int y;
    }

    @Override
    public void actor1(Specimen s) {
        s.x = 1;
        s.y = 1;
    }

    @Override
    public void observe(Specimen s, byte[] res) {
        res[0] = (byte) s.x;
        res[1] = (byte) s.y;
        res[2] = (byte) s.x;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public int resultSize() {
        return 3;
    }

}
