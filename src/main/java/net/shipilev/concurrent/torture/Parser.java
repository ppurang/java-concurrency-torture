package net.shipilev.concurrent.torture;


import net.shipilev.concurrency.torture.schema.descr.Case;
import net.shipilev.concurrency.torture.schema.descr.OutcomeType;
import net.shipilev.concurrency.torture.schema.descr.Test;
import net.shipilev.concurrency.torture.schema.descr.Testsuite;
import net.shipilev.concurrency.torture.schema.result.Result;
import net.shipilev.concurrency.torture.schema.result.Results;
import net.shipilev.concurrency.torture.schema.result.State;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    private final String src;
    private final Map<String, Test> descriptions;

    public Parser(Options opts) throws JAXBException, FileNotFoundException {
        src = opts.getResultFile();
        descriptions = new HashMap<String, Test>();
        readDescriptions();
    }

    private void readDescriptions() throws JAXBException {
        Testsuite suite = unmarshal(Testsuite.class, this.getClass().getResourceAsStream("/net/shipilev/concurrency/torture/test-descriptions.xml"));

        for (Test t : suite.getTest()) {
            descriptions.put(t.getName(), t);
        }
    }

    public void parseHTML() throws FileNotFoundException, JAXBException {
        PrintWriter output = new PrintWriter("results.html");

        Results result = unmarshal(Results.class, new FileInputStream(src));

        for (Result r : result.getResult()) {

            output.println("<h2>" + r.getName() + "</h2>");

            Test test = descriptions.get(r.getName());
            if (test == null) {
                output.println("Missing description for " + r.getName());
                continue;
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

        output.println("<p>Please report the errors in test grading to <a href='https://github.com/shipilev/java-concurrency-torture'>https://github.com/shipilev/java-concurrency-torture</a></p>");

        output.close();
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

    public void parseText(PrintWriter output, Result r) {
//        output.println(r.getName());

        Test test = descriptions.get(r.getName());
        if (test == null) {
            output.println("Missing description for " + r.getName());
            return;
        }

        output.printf("%35s %12s %20s %-20s\n", "Observed state", "Occurrences", "Outcome", "Interpretation");


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
                            c.getOutcome(),
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
                            c.getOutcome(),
                            cutoff(c.getDescription()));
                }
            }
        }

        for (State s : unmatchedStates) {
            output.printf("%35s (%10d) %20s %-40s\n",
                    s.getId(),
                    s.getCount(),
                    test.getUnmatched().getOutcome(),
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
