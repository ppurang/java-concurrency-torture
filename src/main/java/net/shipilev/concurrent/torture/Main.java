package net.shipilev.concurrent.torture;

import com.google.common.base.Predicate;
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

        Parser p = new Parser(opts);

        if (opts.shouldParse()) {
            System.out.println("Re-interpreting the results...");
            System.out.println("Look in results.html for the results");
            System.out.println();

            p.parseHTML();
        } else {
            System.out.println("Running each test for " + opts.getTime() + "ms");
            System.out.println("Each test does " + opts.getLoops() + " internal loops");
            System.out.println("Look in results.html for the results");
            System.out.println();

            Runner r = new Runner(p, opts);

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
