package net.shipilev.concurrent.torture.evaluators;

import net.shipilev.concurrent.torture.Outcome;

public class SingleValueExpected implements Evaluator {

    private final byte value;

    public SingleValueExpected(int value) {
        this.value = (byte) (value & 0xFF);
    }

    @Override
    public int resultSize() {
        return 1;
    }

    @Override
    public Outcome test(byte[] result) {
        if (result[0] == value) {
            return Outcome.EXPECTED;
        } else {
            return Outcome.NOT_EXPECTED;
        }
    }
}
