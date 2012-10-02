package net.shipilev.concurrent.torture;

import net.shipilev.concurrent.torture.negative.DoubleAtomicityTest;
import net.shipilev.concurrent.torture.negative.LongAtomicityTest;
import net.shipilev.concurrent.torture.positive.AtomicIntegerIncrementTest;
import net.shipilev.concurrent.torture.positive.IntAtomicityTest;
import net.shipilev.concurrent.torture.positive.VolatileLongAtomicityTest;
import net.shipilev.concurrent.torture.negative.UnsafeSingletonTest;
import net.shipilev.concurrent.torture.negative.VolatileAtomicityTest;
import net.shipilev.concurrent.torture.positive.ReadAfterVolatileReadTest;
import net.shipilev.concurrent.torture.positive.ReadTwiceOverVolatileReadTest;

import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Java Concurrency Torture Tests");
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("Look up the Javadoc for test to look up the details about the test.");
        System.out.println("Running each test for " + Constants.TIME_MSEC + "ms, -Dtime=# to override.");
        System.out.println("Each test does " + Constants.LOOPS + " internal loops, -Dloops=# to override.");
        System.out.println();

        System.out.println("*** NEGATIVE TESTS (expected to fail) ***");
        new UnsafeSingletonTest().run();
        new VolatileAtomicityTest().run();
        new LongAtomicityTest().run();
        new DoubleAtomicityTest().run();

        System.out.println();
        System.out.println("*** POSITIVE TESTS (expected to pass) ***");
        new AtomicIntegerIncrementTest().run();
        new IntAtomicityTest().run();
        new ReadTwiceOverVolatileReadTest().run();
        new ReadAfterVolatileReadTest().run();
        new VolatileLongAtomicityTest().run();
    }

}
