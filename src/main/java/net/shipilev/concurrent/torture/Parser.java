package net.shipilev.concurrent.torture;


import net.shipilev.concurrency.torture.schema.result.Results;
import net.shipilev.concurrency.torture.schema.result.State;
import net.shipilev.concurrency.torture.schema.result.Result;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.InputStream;

public class Parser {

    private final String resultFile;

    public Parser(Options opts) {
        resultFile = opts.getResultFile();
    }

    public void parse() {
        try {
            Results result = unmarshal(Results.class, new FileInputStream(resultFile));

            for (Result r : result.getResult()) {
                System.err.println(r.getName());
                for (State s : r.getState()) {
                    System.err.println(s.getId() + " " + s.getCount());
                }
            }

        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public <T> T unmarshal( Class<T> docClass, InputStream inputStream )
            throws JAXBException {
        String packageName = docClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();

        @SuppressWarnings("unchecked")
        T unmarshal = (T) u.unmarshal(inputStream);

        return unmarshal;
    }

}
