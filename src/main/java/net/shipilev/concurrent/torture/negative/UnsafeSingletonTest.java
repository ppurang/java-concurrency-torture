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
public class UnsafeSingletonTest implements OneActorOneObserverTest<UnsafeSingletonTest.SingletonFactory> {

    public static class SingletonFactory {
        private Singleton instance; // specifically non-volatile

        public Singleton getInstance() {
            if (instance == null) {
                synchronized (this) {
                    if (instance == null) {
                        instance = new Singleton();
                    }
                }
            }
            return instance;
        }
    }

    public static class Singleton {
        private Byte x;
        public Singleton() { x = 42; }
    }

    @Override
    public void actor1(SingletonFactory s) {
        s.getInstance();
    }

    @Override
    public void observe(SingletonFactory s, byte[] res) {
        Singleton singleton = s.getInstance();
        if (singleton == null) {
            res[0] = 0;
            return;
        }

        if (singleton.x == null) {
            res[0] = 1;
            return;
        }

        res[0] = singleton.x;
    }

    @Override
    public SingletonFactory newState() {
        return new SingletonFactory();
    }

    @Override
    public Outcome test(byte[] res) {
        if (res[0] != 42) {
            return Outcome.NOT_EXPECTED;
        }
        return Outcome.EXPECTED;
    }

    @Override
    public int resultSize() {
        return 1;
    }

}
