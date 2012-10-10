package net.shipilev.concurrent.torture;

import com.google.common.base.Predicate;
import net.shipilev.concurrent.torture.negative.DoubleAtomicityTest;
import net.shipilev.concurrent.torture.negative.LongAtomicityTest;
import net.shipilev.concurrent.torture.negative.RacyPublicationTest;
import net.shipilev.concurrent.torture.negative.ReadTwiceTest;
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
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException, IllegalAccessException, InstantiationException, JAXBException {
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

        if (opts.shouldParse()) {
            Parser p = new Parser(opts);
            p.parse();
        } else {

            Runner r = new Runner(opts);

            // FIXME: Dodgy raw types, clean up.

            for (Class<? extends OneActorOneObserverTest> test : filterTests(opts.getTestRegexp(), OneActorOneObserverTest.class)) {
                OneActorOneObserverTest<?> instance = test.newInstance();
                r.run(instance);
            }

            for (Class<? extends TwoActorsOneArbiterTest> test : filterTests(opts.getTestRegexp(), TwoActorsOneArbiterTest.class)) {
                TwoActorsOneArbiterTest<?> instance = test.newInstance();
                r.run(instance);
            }

            r.close();
        }
    }

    private static <T> SortedSet<Class<? extends T>> filterTests(final Pattern pattern, Class<T> klass) {
        // God I miss both diamonds and lambdas here.

        Reflections r = new Reflections(
                new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder().include("net.shipilev.concurrent.torture.*"))
                        .filterInputsBy(new Predicate<String>() {
                            @Override
                            public boolean apply(@Nullable String s) {
                                return pattern.matcher(s).matches();
                            }
                        })
                        .setUrls(ClasspathHelper.forClassLoader())
                        .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));

        SortedSet<Class<? extends T>> s = new TreeSet<Class<? extends T>>(new Comparator<Class<? extends T>>() {
            @Override
            public int compare(Class<? extends T> o1, Class<? extends T> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        s.addAll(r.getSubTypesOf(klass));
        return s;
    }

}
