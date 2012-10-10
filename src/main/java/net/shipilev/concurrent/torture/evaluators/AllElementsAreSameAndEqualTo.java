package net.shipilev.concurrent.torture.evaluators;

import net.shipilev.concurrent.torture.Outcome;

public class AllElementsAreSameAndEqualTo implements Evaluator {

    private final int size;
    private final byte[] expectedValues;

    public AllElementsAreSameAndEqualTo(int size, int... values) {
        if (size < 1) throw new IllegalArgumentException("size < 1");
        this.size = size;

        expectedValues = new byte[values.length];
        for (int c = 0; c < values.length; c++) {
            expectedValues[c] = (byte) (values[c] & 0xFF);
        }

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

        for (byte expected : expectedValues) {
            if (f == expected) {
                return Outcome.ACCEPTABLE;
            }
        }
        return Outcome.NOT_EXPECTED;
    }


}
