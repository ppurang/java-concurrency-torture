package net.shipilev.concurrent.torture.tests.init;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

/**
 * Tests if final primitive longs experience tearing when initializing in constructor.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class LongConstrTest implements OneActorOneObserverTest<LongConstrTest.Specimen> {

    public static class Specimen {
        Shell shell;
    }

    public static class Shell {
        long x;

        public Shell() {
            this.x = 0xFFFFFFFFFFFFFFFFL;
        }
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen s) {
        s.shell = new Shell();
    }

    @Override
    public void observe(Specimen s, byte[] result) {
        if (s.shell == null) {
            result[0] = 42;
            result[1] = 42;
            result[2] = 42;
            result[3] = 42;
            result[4] = 42;
            result[5] = 42;
            result[6] = 42;
            result[7] = 42;
        } else {
            long t = s.shell.x;
            result[0] = (byte) ((t >> 0) & 0xFF);
            result[1] = (byte) ((t >> 8) & 0xFF);
            result[2] = (byte) ((t >> 16) & 0xFF);
            result[3] = (byte) ((t >> 24) & 0xFF);
            result[4] = (byte) ((t >> 32) & 0xFF);
            result[5] = (byte) ((t >> 40) & 0xFF);
            result[6] = (byte) ((t >> 48) & 0xFF);
            result[7] = (byte) ((t >> 56) & 0xFF);
        }
    }

    @Override
    public int resultSize() {
        return 8;
    }

}