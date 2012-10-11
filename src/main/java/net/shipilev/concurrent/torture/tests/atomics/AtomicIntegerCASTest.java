package net.shipilev.concurrent.torture.tests.atomics;

import net.shipilev.concurrent.torture.tests.TwoActorsOneArbiterTest;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the atomicity of AtomicInteger.compareAndSet.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class AtomicIntegerCASTest implements TwoActorsOneArbiterTest<AtomicInteger> {

    @Override
    public void actor1(AtomicInteger s) {
        s.compareAndSet(0, 1);
    }

    @Override
    public void actor2(AtomicInteger s) {
        s.compareAndSet(0, 2);
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
