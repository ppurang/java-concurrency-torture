package net.shipilev.concurrent.torture.tests.singletons;


/**
 * Tests the singleton factory.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class FinalWrapperSingletonTest extends AbstractSingletonTest {

    public static class FinalWrapperFactory implements SingletonFactory {
        private FinalWrapper wrapper;

        public Singleton getInstance() {
            if (wrapper == null) {
                synchronized(this) {
                    if (wrapper == null) {
                        wrapper = new FinalWrapper(new Singleton());
                    }
                }
            }
            return wrapper.instance;
        }

        private static class FinalWrapper {
            public final Singleton instance;
            public FinalWrapper(Singleton instance) {
                this.instance = instance;
            }
        }
    }

    @Override
    public SingletonFactory newState() {
        return new FinalWrapperFactory();
    }

}
