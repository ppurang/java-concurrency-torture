package net.shipilev.concurrent.torture.positive;

import net.shipilev.concurrent.torture.evaluators.Evaluator;
import net.shipilev.concurrent.torture.TwoActorsOneArbiterTest;
import net.shipilev.concurrent.torture.evaluators.SingleValueExpected;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the atomicity of AtomicInteger incrementAndGet.
 * This is guaranteed by JMM and implementation.
 * The failure on this scenario highlights the possible bug.
 *
 * Possible observed states are:
 *    [0]: INCORRECT: complete infrastructure failure (this is an infrastructure bug to have one)
 *    [1]: INCORRECT: lost update
 *    [2]: CORRECT:   both updates are intact
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class AtomicIntegerIncrementTest implements TwoActorsOneArbiterTest<AtomicInteger> {

    @Override
    public void actor1(AtomicInteger s) {
        s.incrementAndGet();
    }

    @Override
    public void actor2(AtomicInteger s) {
        s.incrementAndGet();
    }

    @Override
    public void arbitrate(AtomicInteger s, byte[] result) {
        result[0] = (byte) s.get();
    }

    @Override
    public AtomicInteger newState() {
        return new AtomicInteger();
    }

    @Override
    public Evaluator getEvaluator() {
        return new SingleValueExpected(2);
    }

}
