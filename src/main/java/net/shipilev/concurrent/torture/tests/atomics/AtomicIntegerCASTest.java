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

import net.shipilev.concurrent.torture.tests.TwoActorsOneArbiterTest;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the atomicity of AtomicInteger.compareAndSet.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class AtomicIntegerCASTest implements TwoActorsOneArbiterTest<AtomicInteger> {

    @Override
    public void actor1(AtomicInteger s) {
        s.compareAndSet(0, 1);
    }

    @Override
    public void actor2(AtomicInteger s) {
        s.compareAndSet(0, 2);
    }

    @Override
    public void arbitrate(AtomicInteger s, byte[] result) {
        result[0] = (byte) s.get();
    }

    @Override
    public AtomicInteger newState() {
        return new AtomicInteger();
    }

    @Override
    public int resultSize() {
        return 1;
    }

}
