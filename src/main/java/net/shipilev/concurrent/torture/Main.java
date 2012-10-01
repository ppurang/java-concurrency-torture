package net.shipilev.concurrent.torture;

import net.shipilev.concurrent.torture.positive.AtomicRaceTest;
import net.shipilev.concurrent.torture.negative.IntTearingTest;
import net.shipilev.concurrent.torture.negative.LongTearingTest;
import net.shipilev.concurrent.torture.negative.SyncBarrierTest;
import net.shipilev.concurrent.torture.negative.UnsafeSingletonTest;
import net.shipilev.concurrent.torture.negative.VolatileAtomicityTest;
import net.shipilev.concurrent.torture.positive.VolatileLongTearingTest;
import net.shipilev.concurrent.torture.positive.VolatileReadTest;
import net.shipilev.concurrent.torture.positive.VolatileWriteTest;

import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("*** NEGATIVE TESTS (expected to fail) ***");
        new UnsafeSingletonTest().run();
        new SyncBarrierTest().run();
        new VolatileAtomicityTest().run();
        new AtomicRaceTest().run();
        new IntTearingTest().run();
        new LongTearingTest().run();

        System.out.println();
        System.out.println("*** POSITIVE TESTS (expected to pass) ***");
        new VolatileReadTest().run();
        new VolatileWriteTest().run();
        new VolatileLongTearingTest().run();
    }

}
