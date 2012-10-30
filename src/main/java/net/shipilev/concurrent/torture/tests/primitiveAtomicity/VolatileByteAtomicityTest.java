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

package net.shipilev.concurrent.torture.tests.primitiveAtomicity;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

/**
 * Tests if primitive bytes experience non-atomic reads/writes.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class VolatileByteAtomicityTest implements OneActorOneObserverTest<VolatileByteAtomicityTest.Specimen> {

    public static class Specimen {
        volatile byte x;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen s) {
        s.x = (byte)0xFF;
    }

    @Override
    public void observe(Specimen s, byte[] result) {
        byte b = s.x;
        result[0] = (byte)((b >> 0) & 0xF);
        result[1] = (byte)((b >> 4) & 0xF);
    }

    @Override
    public int resultSize() {
        return 2;
    }

}
