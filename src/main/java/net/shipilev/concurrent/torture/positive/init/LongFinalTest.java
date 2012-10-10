package net.shipilev.concurrent.torture.positive.init;

import net.shipilev.concurrent.torture.evaluators.Evaluator;
import net.shipilev.concurrent.torture.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.evaluators.AllElementsAreSameAndEqualTo;

/**
 * Tests if final primitive longs experience tearing when initializing as final field.
 * TODO: Is this a forbidden behavior?
 * The failure on this test highlights the possible bug.
 *
 * Possible observed states:
 *    - CORRECT:   shell == null
 *    - INCORRECT: default value for long (i.e. 0)
 *    - CORRECT:   value set by constructor (i.e. -1)
 *    - INCORRECT: low-word set to -1, high-word still set to 0 (tearing)
 *    - INCORRECT: high-word set to -1, low-word still set to 0 (tearing)
 *
 * All other values are forbidden because out-of-thin-air values are forbidden.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class LongFinalTest implements OneActorOneObserverTest<LongFinalTest.Specimen> {

    public static class Specimen {
        Shell shell;
    }

    public static class Shell {
        final long x;

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
    public Evaluator getEvaluator() {
        return new AllElementsAreSameAndEqualTo(8, 42, -1);
    }

}