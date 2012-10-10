package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.evaluators.Evaluator;
import net.shipilev.concurrent.torture.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.Outcome;

/**
 * Test if volatile write-read induces happens-before if in between two non-volatile reads.
 *
 * Possible outcomes:
 *   [*, 0, *]: CORRECT: volatile write to $y is not yet visible, $x could be whatever
 *   [0, 1, 0]: INCORRECT: volatile write to $y had happened, and update to $x had been lost
 *   [1, 1, 0]: INCORRECT: volatile write to $y had happened, and update to $x had been lost (this one is very weird)
 *   [0, 1, 1]: CORRECT: volatile write to $y had happened, and update to $x had been read
 *   [1, 1, 1]: CORRECT: volatile write to $y had happened, and update to $x had been read even before
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
    public Evaluator getEvaluator() {
        return new Tester();
    }

    public static class Tester implements Evaluator {

        @Override
        public Outcome test(byte[] res) {
            if (res[1] == 1 && res[2] == 0) {
                return Outcome.NOT_EXPECTED;
            }
            return Outcome.ACCEPTABLE;
        }

        public int resultSize() {
            return 3;
        }

    }

}
