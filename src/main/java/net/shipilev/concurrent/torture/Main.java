package net.shipilev.concurrent.torture;

import net.shipilev.concurrent.torture.tests.ConcurrencyTest;
import net.shipilev.concurrent.torture.tests.OneActorOneObserverTest;
import net.shipilev.concurrent.torture.tests.TwoActorsOneArbiterTest;
import net.shipilev.concurrent.torture.util.InputStreamDrainer;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
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

        if (opts.shouldParse()) {
            System.out.println("Re-interpreting the results...");
            System.out.println("Look in results.html for the results");
            System.out.println();

            XMLtoHTMLResultPrinter p = new XMLtoHTMLResultPrinter(opts);
            p.parse();
        } else {
            if (opts.shouldFork()) {
                for (Class<? extends ConcurrencyTest> test : filterTests(opts.getTestFilter(), OneActorOneObserverTest.class)) {
                    runForked(opts, test);
                }
                for (Class<? extends ConcurrencyTest> test : filterTests(opts.getTestFilter(), TwoActorsOneArbiterTest.class)) {
                    runForked(opts, test);
                }
            } else {
                runAll(opts);
            }
        }
    }

    private static void runForked(Options opts, Class<? extends ConcurrencyTest>  test) {
        try {
            String commandString = getSeparateExecutionCommand(opts, test.getName());
//            System.err.println("Invoking: " + commandString);
            Process p = Runtime.getRuntime().exec(commandString);

            InputStreamDrainer errDrainer = new InputStreamDrainer(p.getErrorStream(), System.err);
            InputStreamDrainer outDrainer = new InputStreamDrainer(p.getInputStream(), System.out);

            errDrainer.start();
            outDrainer.start();

            int ecode = p.waitFor();

            if (ecode != 0) {
                throw new IllegalStateException("WARNING: Forked process returned code: " + ecode);
            }

            errDrainer.join();
            outDrainer.join();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private static void runAll(Options opts) throws FileNotFoundException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException, JAXBException {
        System.out.println("Running each test for " + opts.getTime() + "ms");
        System.out.println("Each test does " + opts.getLoops() + " internal loops");
        System.out.println("Look in results.html for the results");
        System.out.println();

        Runner r = new Runner(opts);

        for (Class<? extends OneActorOneObserverTest> test : filterTests(opts.getTestFilter(), OneActorOneObserverTest.class)) {
            OneActorOneObserverTest<?> instance = test.newInstance();
            r.run(instance);
        }

        for (Class<? extends TwoActorsOneArbiterTest> test : filterTests(opts.getTestFilter(), TwoActorsOneArbiterTest.class)) {
            TwoActorsOneArbiterTest<?> instance = test.newInstance();
            r.run(instance);
        }

        r.close();
    }

    public static String getSeparateExecutionCommand(Options opts, String test) {
        Properties props = System.getProperties();
        String javaHome = (String) props.get("java.home");
        String separator = File.separator;
        String osName = props.getProperty("os.name");
        boolean isOnWindows = osName.contains("indows");
        String platformSpecificBinaryPostfix = isOnWindows ? ".exe" : "";

        String classPath = (String) props.get("java.class.path");

        if (isOnWindows) {
            classPath = '"' + classPath + '"';
        }

        // else find out which one parent is and use that
        StringBuilder javaExecutable = new StringBuilder();
        javaExecutable.append(javaHome);
        javaExecutable.append(separator);
        javaExecutable.append("bin");
        javaExecutable.append(separator);
        javaExecutable.append("java");
        javaExecutable.append(platformSpecificBinaryPostfix);
        String javaExecutableString = javaExecutable.toString();


        // else use same jvm args given to this runner
        StringBuilder jvmArguments = new StringBuilder();
        RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
        List<String> args = RuntimemxBean.getInputArguments();

        for (String arg : args) {
            jvmArguments.append(arg);
            jvmArguments.append(' ');
        }
        if (jvmArguments.length() > 0) {
            jvmArguments.setLength(jvmArguments.length() - 1);
        }

        String jvmArgumentsString = jvmArguments.toString();

        // assemble final process command

        StringBuilder command = new StringBuilder();
        command.append(javaExecutableString);

        if (!jvmArgumentsString.isEmpty()) {
            command.append(' ');
            command.append(jvmArgumentsString);
        }

        command.append(" -cp ");
        command.append(classPath);
        command.append(' ');
        command.append(ForkedMain.class.getName());

        return command.toString() + " " + opts.buildForkedCmdLine() + " -t " + test;
    }


    private static <T> SortedSet<Class<? extends T>> filterTests(final String filter, Class<T> klass) {
        // God I miss both diamonds and lambdas here.

        Pattern pattern = Pattern.compile(filter);

        Reflections r = new Reflections(
                new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder().include("net.shipilev.concurrent.torture.*"))
                        .setUrls(ClasspathHelper.forClassLoader())
                        .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));

        SortedSet<Class<? extends T>> s = new TreeSet<Class<? extends T>>(new Comparator<Class<? extends T>>() {
            @Override
            public int compare(Class<? extends T> o1, Class<? extends T> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (Class<? extends T> k : r.getSubTypesOf(klass)) {
//            System.err.println("matching " + k);
            if (!pattern.matcher(k.getName()).matches()) {
                continue;
            }
            if (Modifier.isAbstract(k.getModifiers())) {
                continue;
            }
            s.add(k);
        }

        return s;
    }

}
