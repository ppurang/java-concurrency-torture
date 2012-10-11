/*
 * Copyright (c) 2012 Aleksey Shipilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
