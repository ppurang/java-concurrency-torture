package net.shipilev.concurrent.torture.negative;

import net.shipilev.concurrent.torture.Outcome;
import net.shipilev.concurrent.torture.OneActorOneObserverTest;

public class UnsafeSingletonTest extends OneActorOneObserverTest<UnsafeSingletonTest.SingletonFactory> {

    public static class SingletonFactory {

        private Singleton instance;

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
    public SingletonFactory createNew() {
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
    protected int resultSize() {
        return 1;
    }

}
