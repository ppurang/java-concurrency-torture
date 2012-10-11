package net.shipilev.concurrent.torture.tests.singletons;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;

/**
 * Tests the broken double-checked locking.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public abstract class AbstractSingletonTest implements OneActorOneObserverTest<AbstractSingletonTest.SingletonFactory> {

    @Override
    public final void actor1(SingletonFactory s) {
        s.getInstance();
    }

    @Override
    public final void observe(SingletonFactory s, byte[] res) {
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
    public abstract SingletonFactory newState();

    @Override
    public final int resultSize() {
        return 1;
    }

    public interface SingletonFactory {
        Singleton getInstance();
    }

    public static class Singleton {
        private Byte x;
        public Singleton() { x = 42; }
    }

}
