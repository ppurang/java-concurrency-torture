package net.shipilev.concurrent.torture;


import com.google.common.base.Predicate;
import net.shipilev.concurrency.torture.schema.descr.Case;
import net.shipilev.concurrency.torture.schema.descr.OutcomeType;
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
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XMLtoHTMLResultPrinter {

    private final String resultDir;
    private final Map<String, Test> descriptions;

    public XMLtoHTMLResultPrinter(Options opts) throws JAXBException, FileNotFoundException {
        resultDir = opts.getResultDest();
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

    public void parse() throws FileNotFoundException, JAXBException {
        PrintWriter output = new PrintWriter(resultDir + "/index.html");

        output.println("<html>");
        output.println("<head><title>Java Concurrency Torture report</title></head>");
        output.println("<body>");

        File[] files = new File(resultDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("xml");
            }
        });

        for (File f : files) {
            parse(output, unmarshal(Result.class, new FileInputStream(f)));
        }

        output.println("<p>Please report the errors in test grading to <a href='https://github.com/shipilev/java-concurrency-torture/issues'>https://github.com/shipilev/java-concurrency-torture/issues</a></p>");

        output.println("</body>");
        output.println("</html>");

        output.close();
    }

    public void parse(PrintWriter output, Result r) throws FileNotFoundException, JAXBException {

        output.println("<h2>" + r.getName() + "</h2>");

        Test test = descriptions.get(r.getName());
        if (test == null) {
            output.println("Missing description for " + r.getName());
            System.err.println("Missing description for " + r.getName());
            return;
        }

        output.println("<p>" + test.getDescription() + "</p>");

        output.println("<table width=1000>");
        output.println("<tr>");
        output.println("<th width=250>Observed state</th>");
        output.println("<th width=50>Occurence</th>");
        output.println("<th width=50>Outcome</th>");
        output.println("<th width=600>Interpretation</th>");
        output.println("</tr>");

        List<State> unmatchedStates = new ArrayList<State>();
        unmatchedStates.addAll(r.getState());
        for (Case c : test.getCase()) {

            boolean matched = false;

            for (State s : r.getState()) {
                if (c.getMatch().contains(s.getId())) {
                    // match!
                    output.println("<tr bgColor=" + selectHTMLColor(c.getOutcome(), s.getCount() == 0) + ">");
                    output.println("<td>" + s.getId() + "</td>");
                    output.println("<td align=center>" + s.getCount() + "</td>");
                    output.println("<td align=center>" + c.getOutcome() + "</td>");
                    output.println("<td>" + c.getDescription() + "</td>");
                    output.println("</tr>");
                    matched = true;
                    unmatchedStates.remove(s);
                }
            }

            if (!matched) {
                for (String m : c.getMatch()) {
                    output.println("<tr bgColor=" + selectHTMLColor(c.getOutcome(), true) + ">");
                    output.println("<td>" + m + "</td>");
                    output.println("<td align=center>" + 0 + "</td>");
                    output.println("<td align=center>" + c.getOutcome() + "</td>");
                    output.println("<td>" + c.getDescription() + "</td>");
                    output.println("</tr>");
                }
            }
        }

        for (State s : unmatchedStates) {
            output.println("<tr bgColor=" + selectHTMLColor(test.getUnmatched().getOutcome(), s.getCount() == 0) + ">");
            output.println("<td>" + s.getId() + "</td>");
            output.println("<td align=center>" + s.getCount() + "</td>");
            output.println("<td align=center>" + test.getUnmatched().getOutcome() + "</td>");
            output.println("<td>" + test.getUnmatched().getDescription() + "</td>");
            output.println("</tr>");
        }

        output.println("</table>");
    }

    public String selectHTMLColor(OutcomeType type, boolean isZero) {
        String rgb = Integer.toHexString(selectColor(type, isZero).getRGB());
        return "#" + rgb.substring(2, rgb.length());
    }

    public Color selectColor(OutcomeType type, boolean isZero) {
        switch (type) {
            case POSITIVE_REQUIRED:
                return isZero ? Color.RED : Color.GREEN;
            case POSITIVE_MISSING:
                return isZero ? Color.LIGHT_GRAY : Color.RED;
            case NEGATIVE_REQUIRED:
                return isZero ? Color.CYAN : Color.GREEN;
            case NEGATIVE_MISSING:
                return isZero ? Color.LIGHT_GRAY : Color.CYAN;
            case ACCEPTABLE:
                return isZero ? Color.LIGHT_GRAY : Color.GREEN;
            default:
                throw new IllegalStateException();
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

}
