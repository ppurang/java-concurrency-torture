package net.shipilev.concurrent.torture.tests.atomics;

import net.shipilev.concurrent.torture.TwoActorsOneArbiterTest;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the atomicity of AtomicInteger incrementAndGet.
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
    public int resultSize() {
        return 1;
    }

}
