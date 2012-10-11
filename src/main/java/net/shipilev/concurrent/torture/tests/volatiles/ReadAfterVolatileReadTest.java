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

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

/**
 * Tests if volatile write-read induce proper happens-before.
 *
 *  @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class ReadAfterVolatileReadTest implements OneActorOneObserverTest<ReadAfterVolatileReadTest.Specimen> {

    public static class Specimen {
        int x;
        volatile int y;
    }

    @Override
    public void actor1(Specimen s) {
        s.x = 1;
        s.x = 2;
        s.y = 1;
        s.x = 3;
    }

    @Override
    public void observe(Specimen s, byte[] res) {
        res[0] = (byte) s.y;
        res[1] = (byte) s.x;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public int resultSize() {
        return 2;
    }

}
