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

package net.shipilev.concurrent.torture.tests.atomics;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.tests.TwoActorsOneArbiterTest;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the atomicity of AtomicInteger.compareAndSet.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class AtomicIntegerInitialValueTest implements OneActorOneObserverTest<AtomicIntegerInitialValueTest.Shell> {

    public static class Shell {
        private AtomicInteger ai;
    }

    @Override
    public void actor1(Shell s) {
        s.ai = new AtomicInteger(1);
    }

    @Override
    public void observe(Shell s, byte[] result) {
        AtomicInteger ai = s.ai;
        result[0] = (ai == null) ? -1 : (byte) ai.get();
    }

    @Override
    public Shell newState() {
        return new Shell();
    }

    @Override
    public int resultSize() {
        return 1;
    }

}
