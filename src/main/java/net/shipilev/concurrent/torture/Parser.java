package net.shipilev.concurrent.torture;


import net.shipilev.concurrency.torture.schema.descr.Case;
import net.shipilev.concurrency.torture.schema.descr.Test;
import net.shipilev.concurrency.torture.schema.descr.Testsuite;
import net.shipilev.concurrency.torture.schema.result.Result;
import net.shipilev.concurrency.torture.schema.result.Results;
import net.shipilev.concurrency.torture.schema.result.State;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
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

    public void parse() throws FileNotFoundException, JAXBException {
        PrintWriter output = new PrintWriter("result.html");

        Results result = unmarshal(Results.class, new FileInputStream(src));

        for (Result r : result.getResult()) {

            output.println("<p>" + r.getName() + "</p>");

            Test test = descriptions.get(r.getName());
            if (test == null) {
                output.println("Missing description for " + r.getName());
                continue;
            }

            output.println("<table>");
            output.println("<tr>");
            output.println("<th>Observed state</th>");
            output.println("<th>Occurence</th>");
            output.println("<th>Outcome</th>");
            output.println("<th>Interpretation</th>");
            output.println("</tr>");

            for (Case c : test.getCase()) {
                for (State s : r.getState()) {
                    if (c.getMatch().contains(s.getId())) {
                        // match!
                        output.println("<tr>");
                        output.println("<td>" + s.getId() + "</td>");
                        output.println("<td>" + s.getCount() + "</td>");
                        output.println("<td>" + c.getOutcome() + "</td>");
                        output.println("<td>" + c.getDescription() + "</td>");
                        output.println("</tr>");
                    }
                }
            }
            output.println("</table>");

        }

        output.close();
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
