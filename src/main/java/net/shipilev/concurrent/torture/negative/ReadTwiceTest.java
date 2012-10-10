package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.evaluators.Evaluator;
import net.shipilev.concurrent.torture.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.Outcome;

/**
 * Test if intervening write breaks reordering.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class ReadTwiceTest implements OneActorOneObserverTest<ReadTwiceTest.Specimen> {

    public static class Specimen {
        int x;
        int y;
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
