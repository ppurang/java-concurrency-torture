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

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

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
