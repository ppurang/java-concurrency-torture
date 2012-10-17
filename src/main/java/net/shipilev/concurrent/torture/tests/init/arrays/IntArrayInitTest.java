package net.shipilev.concurrent.torture.tests.init.arrays;

import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;

public class IntArrayInitTest implements OneActorOneObserverTest<IntArrayInitTest.Specimen> {

    public static class Specimen {
        int[] arr;
    }

    @Override
    public Specimen newState() {
        return new Specimen();
    }

    @Override
    public void actor1(Specimen state) {
        state.arr = new int[10];
    }

    @Override
    public void observe(Specimen state, byte[] result) {
        int[] arr = state.arr;
        if (arr == null) {
            result[0] = -1;
        } else {
            for (int i : arr) {
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
