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

package net.shipilev.concurrent.torture.tests.init;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

/**
 * Tests if final primitive longs experience tearing when initializing as final field.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class LongFinalTest implements OneActorOneObserverTest<LongFinalTest.Specimen> {

    public static class Specimen {
        Shell shell;
    }

    public static class Shell {
        final long x;

        public Shell() {
            this.x = 0xFFFFFFFFFFFFFFFFL;
        }
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen s) {
        s.shell = new Shell();
    }

    @Override
    public void observe(Specimen s, byte[] result) {
        if (s.shell == null) {
            result[0] = 42;
            result[1] = 42;
            result[2] = 42;
            result[3] = 42;
            result[4] = 42;
            result[5] = 42;
            result[6] = 42;
            result[7] = 42;
        } else {
            long t = s.shell.x;
            result[0] = (byte) ((t >> 0) & 0xFF);
            result[1] = (byte) ((t >> 8) & 0xFF);
            result[2] = (byte) ((t >> 16) & 0xFF);
            result[3] = (byte) ((t >> 24) & 0xFF);
            result[4] = (byte) ((t >> 32) & 0xFF);
            result[5] = (byte) ((t >> 40) & 0xFF);
            result[6] = (byte) ((t >> 48) & 0xFF);
            result[7] = (byte) ((t >> 56) & 0xFF);
        }
    }

    @Override
    public int resultSize() {
        return 8;
    }

}