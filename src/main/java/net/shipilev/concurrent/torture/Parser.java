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
import java.util.HashMap;
import java.util.Map;

public class Parser {

    private final String resultFile;
    private final Map<String, Test> descriptions;

    public Parser(Options opts) throws JAXBException {
        resultFile = opts.getResultFile();
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
        Results result = unmarshal(Results.class, new FileInputStream(resultFile));

        for (Result r : result.getResult()) {

            Test test = descriptions.get(r.getName());
            if (test == null) {
                System.err.println("Missing description for " + r.getName());
                continue;
            }

            for (Case c : test.getCase()) {
                for (State s : r.getState()) {
                    if (c.getMatch().contains(s.getId())) {
                        // match!
                        System.err.println(s.getId() + " " + s.getCount() + " " + c.getOutcome() + " " + c.getDescription());
                    }
                }
            }
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
