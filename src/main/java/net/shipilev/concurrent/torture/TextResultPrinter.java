package net.shipilev.concurrent.torture;


import com.google.common.base.Predicate;
import net.shipilev.concurrency.torture.schema.descr.Case;
import net.shipilev.concurrency.torture.schema.descr.Test;
import net.shipilev.concurrency.torture.schema.descr.Testsuite;
import net.shipilev.concurrency.torture.schema.result.Result;
import net.shipilev.concurrency.torture.schema.result.State;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TextResultPrinter {

    private final Map<String, Test> descriptions;

    public TextResultPrinter(Options opts) throws JAXBException, FileNotFoundException {
        descriptions = new HashMap<String, Test>();
        readDescriptions();
    }

    private void readDescriptions() throws JAXBException {
        Reflections r = new Reflections(
                new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder().include("net.shipilev.concurrent.torture.desc.*"))
                        .setUrls(ClasspathHelper.forClassLoader())
                        .setScanners(new ResourcesScanner()));

        Set<String> resources = r.getResources(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String s) {
                return s != null && s.endsWith(".xml");
            }
        });

        for (String res : resources) {
            loadDescription(res);
        }
    }

    private void loadDescription(String name) throws JAXBException {
        Testsuite suite = unmarshal(Testsuite.class, this.getClass().getResourceAsStream("/" + name));

        for (Test t : suite.getTest()) {
            descriptions.put(t.getName(), t);
        }
    }

    public <T> T unmarshal(Class<T> docClass, InputStream inputStream)
            throws JAXBException {
        String packageName = docClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();

        @SuppressWarnings("unchecked")
        T unmarshal = (T) u.unmarshal(inputStream);

        return unmarshal;
    }

    public void parse(PrintWriter output, Result r) {
        Test test = descriptions.get(r.getName());
        if (test == null) {
            output.println("Missing description for " + r.getName());
            return;
        }

        output.printf("%35s %12s %20s %-20s\n", "Observed state", "Occurrences", "Expectation", "Interpretation");

        List<State> unmatchedStates = new ArrayList<State>();
        unmatchedStates.addAll(r.getState());
        for (Case c : test.getCase()) {

            boolean matched = false;

            for (State s : r.getState()) {
                if (c.getMatch().contains(s.getId())) {
                    // match!
                    output.printf("%35s (%10d) %20s %-40s\n",
                            s.getId(),
                            s.getCount(),
                            c.getExpect(),
                            cutoff(c.getDescription()));
                    matched = true;
                    unmatchedStates.remove(s);
                }
            }

            if (!matched) {
                for (String m : c.getMatch()) {
                    output.printf("%35s (%10d) %20s %-40s\n",
                            m,
                            0,
                            c.getExpect(),
                            cutoff(c.getDescription()));
                }
            }
        }

        for (State s : unmatchedStates) {
            output.printf("%35s (%10d) %20s %-40s\n",
                    s.getId(),
                    s.getCount(),
                    test.getUnmatched().getExpect(),
                    cutoff(test.getUnmatched().getDescription()));
        }

    }

    private static String cutoff(String src) {
        while (src.contains("  ")) {
            src = src.replaceAll("  ", " ");
        }
        String trim = src.replaceAll("\n", "").trim();
        String substring = trim.substring(0, Math.min(60, trim.length()));
        if (!substring.equals(trim)) {
            return substring + "...";
        } else {
            return substring;
        }
    }

}
