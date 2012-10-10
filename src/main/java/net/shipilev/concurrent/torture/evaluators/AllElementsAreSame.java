package net.shipilev.concurrent.torture.evaluators;

import net.shipilev.concurrent.torture.Outcome;

public class AllElementsAreSame implements Evaluator {

    private final int size;

    public AllElementsAreSame(int size) {
        if (size < 1) throw new IllegalArgumentException("size < 1");
        this.size = size;
    }

    @Override
    public int resultSize() {
        return size;
    }

    @Override
    public Outcome test(byte[] result) {
        byte f = result[0];
        for (int i = 1; i < size; i++) {
            if (f != result[i]) {
                return Outcome.NOT_EXPECTED;
            }
        }
        return Outcome.ACCEPTABLE;
    }


}
