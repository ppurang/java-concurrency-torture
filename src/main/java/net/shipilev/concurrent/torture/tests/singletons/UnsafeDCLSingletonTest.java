package net.shipilev.concurrent.torture.tests.singletons;

/**
 * Tests the unsafe double-checked locking singleton.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class UnsafeDCLSingletonTest extends AbstractSingletonTest {

    public static class UnsafeSingletonFactory implements SingletonFactory {
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

    @Override
    public SingletonFactory newState() {
        return new UnsafeSingletonFactory();
    }

}
