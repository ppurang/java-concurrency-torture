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

package net.shipilev.concurrent.torture.tests.volatiles;

import net.shipilev.concurrent.torture.tests.TwoActorsOneArbiterTest;

/**
 * Tests the atomicity of volatile increment.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class VolatileIncrementAtomicityTest implements TwoActorsOneArbiterTest<VolatileIncrementAtomicityTest.Specimen> {

    public static class Specimen {
        volatile int x;
    }

    @Override
    public void actor1(Specimen s) {
        s.x++;
    }

    @Override
    public void actor2(Specimen s) {
        s.x++;
    }

    @Override
    public void arbitrate(Specimen s, byte[] result) {
        result[0] = (byte) s.x;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public int resultSize() {
        return 1;
    }

}
