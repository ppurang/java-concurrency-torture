package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.Outcome;

/**
 * Tests the broken double-checked locking.
 * This is allowed by JMM, and hence this is a negative test.
 * The failure on this test DOES NOT highlight the possible bug.
 *
 * The race is on getting uninitialized field in Singleton.
 *
 * Note: this is a very fine race, you might need to run longer to observe the failure.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class RacyPublicationTest implements OneActorOneObserverTest<RacyPublicationTest.Specimen> {

    public static class Specimen {
        Shell s;
    }

    public static class Shell {
        int b1, b2, b3, b4, b5, b6, b7, b8;

        public Shell() {
            b1 = b2 = b3 = b4 = b5 = b6 = b7 = b8 = 1;
        }
    }

    @Override
    public void actor1(Specimen s) {
        s.s = new Shell();
    }

    @Override
    public void observe(Specimen s, byte[] res) {
        if (s.s == null) {
            res[0] = res[1] = res[2] = res[3] = res[4] = res[5] = res[6] = res[7] = -1;
        } else {
            res[0] = (byte) (s.s.b1 & 0xFF);
            res[1] = (byte) (s.s.b2 & 0xFF);
            res[2] = (byte) (s.s.b3 & 0xFF);
            res[3] = (byte) (s.s.b4 & 0xFF);
            res[4] = (byte) (s.s.b5 & 0xFF);
            res[5] = (byte) (s.s.b6 & 0xFF);
            res[6] = (byte) (s.s.b7 & 0xFF);
            res[7] = (byte) (s.s.b8 & 0xFF);
        }
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public Outcome test(byte[] res) {
        if (res[0] != res[1] || res[1] != res[2] || res[2] != res[3]
                || res[3] != res[4] || res[5] != res[6] || res[6] != res[7]) {
            return Outcome.NOT_EXPECTED;
        }
        return Outcome.EXPECTED;
    }

    @Override
    public int resultSize() {
        return 8;
    }

}
