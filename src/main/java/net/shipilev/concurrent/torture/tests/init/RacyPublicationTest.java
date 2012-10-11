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
 * Tests the broken double-checked locking.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class RacyPublicationTest implements OneActorOneObserverTest<RacyPublicationTest.Specimen> {

    public static class Specimen {
        Shell s;
    }

    public static class Shell {
        int b1, b2, b3, b4, b5, b6, b7, b8;

        public Shell() {
            b1 = b2 = b3 = b4 = b5 = b6 = b7 = b8 = 1;
        }
    }

    @Override
    public void actor1(Specimen s) {
        s.s = new Shell();
    }

    @Override
    public void observe(Specimen s, byte[] res) {
        Shell shell = s.s;
        if (shell == null) {
            res[0] = res[1] = res[2] = res[3] = res[4] = res[5] = res[6] = res[7] = -1;
        } else {
            res[0] = (byte) (shell.b1 & 0xFF);
            res[1] = (byte) (shell.b2 & 0xFF);
            res[2] = (byte) (shell.b3 & 0xFF);
            res[3] = (byte) (shell.b4 & 0xFF);
            res[4] = (byte) (shell.b5 & 0xFF);
            res[5] = (byte) (shell.b6 & 0xFF);
            res[6] = (byte) (shell.b7 & 0xFF);
            res[7] = (byte) (shell.b8 & 0xFF);
        }
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public int resultSize() {
        return 8;
    }

}
