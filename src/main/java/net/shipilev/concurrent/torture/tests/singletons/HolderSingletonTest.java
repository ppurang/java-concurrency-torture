package net.shipilev.concurrent.torture.tests.singletons;


/**
 * Tests the singleton factory.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class HolderSingletonTest extends AbstractSingletonTest {

    public static class HolderFactory implements SingletonFactory {
        public Singleton getInstance() {
            return Holder.INSTANCE;
        }

        public static class Holder {
            public static final Singleton INSTANCE = new Singleton();
        }
    }

    @Override
    public SingletonFactory newState() {
        return new HolderFactory();
    }

}
