package net.shipilev.concurrent.torture.tests.atomics;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.tests.TwoActorsOneArbiterTest;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the atomicity of AtomicInteger.compareAndSet.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class AtomicIntegerInitialValueTest implements OneActorOneObserverTest<AtomicIntegerInitialValueTest.Shell> {

    public static class Shell {
        private AtomicInteger ai;
    }

    @Override
    public void actor1(Shell s) {
        s.ai = new AtomicInteger(1);
    }

    @Override
    public void observe(Shell s, byte[] result) {
        AtomicInteger ai = s.ai;
        result[0] = (ai == null) ? -1 : (byte) ai.get();
    }

    @Override
    public Shell newState() {
        return new Shell();
    }

    @Override
    public int resultSize() {
        return 1;
    }

}
