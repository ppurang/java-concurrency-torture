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
