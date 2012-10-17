package net.shipilev.concurrent.torture.tests.init.arrays;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

public class ShortArrayInitTest implements OneActorOneObserverTest<ShortArrayInitTest.Specimen> {

    public static class Specimen {
        short[] arr;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen state) {
        state.arr = new short[10];
    }

    @Override
    public void observe(Specimen state, byte[] result) {
        short[] arr = state.arr;
        if (arr == null) {
            result[0] = -1;
        } else {
            for (long i : arr) {
                if (i != 0) {
                    result[0] = 1;
                    return;
                }
            }
            result[0] = 0;
        }
    }

    @Override
    public int resultSize() {
        return 1;
    }

}
