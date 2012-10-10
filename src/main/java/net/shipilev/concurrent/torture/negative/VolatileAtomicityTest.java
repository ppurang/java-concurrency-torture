package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.evaluators.Evaluator;
import net.shipilev.concurrent.torture.TwoActorsOneArbiterTest;
import net.shipilev.concurrent.torture.evaluators.SingleValueExpected;

/**
 * Tests the atomicity of volatile increment.
 * This is not guaranteed by JMM, and hence this is a negative test.
 * The failure on this test DOES NOT highlight the possible bug.
 *
 * Possible observed states are:
 *    [0]: complete infrastructure failure (this is an infrastructure bug to have one)
 *    [1]: lost update
 *    [2]: both updates are intact
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
    public Evaluator getEvaluator() {
        return new SingleValueExpected(2);
    }

}
