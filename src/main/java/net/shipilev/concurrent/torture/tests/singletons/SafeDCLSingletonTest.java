package net.shipilev.concurrent.torture.tests.singletons;

import net.shipilev.concurrent.torture.OneActorOneObserverTest;

/**
 * Tests the safe double-checked locking singleton.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class SafeDCLSingletonTest extends AbstractSingletonTest {

    public static class SafeSingletonFactory implements SingletonFactory {
        private volatile Singleton instance;

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

    @Override
    public SingletonFactory newState() {
        return new SafeSingletonFactory();
    }

}
