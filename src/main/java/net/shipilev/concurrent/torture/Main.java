package net.shipilev.concurrent.torture;

import net.shipilev.concurrent.torture.negative.DoubleAtomicityTest;
import net.shipilev.concurrent.torture.negative.LongAtomicityTest;
import net.shipilev.concurrent.torture.negative.RacyPublicationTest;
import net.shipilev.concurrent.torture.negative.UnsafeSingletonTest;
import net.shipilev.concurrent.torture.negative.VolatileAtomicityTest;
import net.shipilev.concurrent.torture.positive.AtomicIntegerIncrementTest;
import net.shipilev.concurrent.torture.positive.init.LongFinalTest;
import net.shipilev.concurrent.torture.positive.init.LongConstrTest;
import net.shipilev.concurrent.torture.positive.init.LongInstanceTest;
import net.shipilev.concurrent.torture.positive.IntAtomicityTest;
import net.shipilev.concurrent.torture.positive.ReadAfterVolatileReadTest;
import net.shipilev.concurrent.torture.positive.ReadTwiceOverVolatileReadTest;
import net.shipilev.concurrent.torture.positive.VolatileLongAtomicityTest;
import net.shipilev.concurrent.torture.positive.init.LongVolatileTest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        System.out.println("Java Concurrency Torture Tests");
        System.out.println("---------------------------------------------------------------------------------");

        Options opts = new Options(args);
        if (!opts.parse()) {
            System.exit(1);
        }

        System.out.println("Look up the Javadoc for test to look up the details about the test.");
        System.out.println("Running each test for " + opts.getTime() + "ms");
        System.out.println("Each test does " + opts.getLoops() + " internal loops");
        System.out.println();

        Runner r = new Runner(opts);

        System.out.println("*** NEGATIVE TESTS (expected to fail) ***");
        r.run(new RacyPublicationTest());
        r.run(new UnsafeSingletonTest());
        r.run(new VolatileAtomicityTest());
        r.run(new LongAtomicityTest());
        r.run(new DoubleAtomicityTest());

        System.out.println();
        System.out.println("*** POSITIVE TESTS (expected to pass) ***");
        r.run(new LongInstanceTest());
        r.run(new LongConstrTest());
        r.run(new LongFinalTest());
        r.run(new LongVolatileTest());

        r.run(new AtomicIntegerIncrementTest());
        r.run(new IntAtomicityTest());
        r.run(new ReadTwiceOverVolatileReadTest());
        r.run(new ReadAfterVolatileReadTest());
        r.run(new VolatileLongAtomicityTest());

        r.close();
    }

}
