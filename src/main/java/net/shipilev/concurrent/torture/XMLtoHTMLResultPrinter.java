/*
 * Copyright (c) 2012 Aleksey Shipilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shipilev.concurrent.torture;


import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import net.shipilev.concurrency.torture.schema.descr.Case;
import net.shipilev.concurrency.torture.schema.descr.ExpectType;
import net.shipilev.concurrency.torture.schema.descr.Ref;
import net.shipilev.concurrency.torture.schema.descr.Test;
import net.shipilev.concurrency.torture.schema.descr.Testsuite;
import net.shipilev.concurrency.torture.schema.result.Result;
import net.shipilev.concurrency.torture.schema.result.State;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XMLtoHTMLResultPrinter {

    private final String resultDir;
    private final Multimap<String, Test> testSuites;

    public XMLtoHTMLResultPrinter(Options opts) throws JAXBException, FileNotFoundException {
        resultDir = opts.getResultDest();
        testSuites = TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, new Comparator<Test>() {
            @Override
            public int compare(Test o1, Test o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
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
            public boolean apply(String s) {
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
            testSuites.put(suite.getName(), t);
        }
    }

    public void parse() throws FileNotFoundException, JAXBException {
        File[] files = new File(resultDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("xml");
            }
        });

        Arrays.sort(files);

        Map<String, Result> results = new HashMap<String, Result>();

        for (File f : files) {
            Result r = unmarshal(Result.class, new FileInputStream(f));
            results.put(r.getName(), r);
        }

        PrintWriter output = new PrintWriter(resultDir + "/index.html");

        output.println("<html>");
        output.println("<head><title>Java Concurrency Torture report</title></head>");
        output.println("<body>");

        for (String k : testSuites.keySet()) {
            Collection<Test> tests = testSuites.get(k);

            output.println("<h1>Suite \"" + k + "\"</h1>");

            for (Test test : tests) {
                Result result = results.get(test.getName());
                if (result != null) {
                    parse(output, result, test);
                } else {
                    output.println("Missing description for " + test.getName());
                }
            }

        }

//        output.println("Missing description for " + r.getName());
//        System.err.println("Missing description for " + r.getName());

        output.println("<p>Please report the errors in test grading to <a href='https://github.com/shipilev/java-concurrency-torture/issues'>https://github.com/shipilev/java-concurrency-torture/issues</a></p>");

        output.println("</body>");
        output.println("</html>");

        output.close();
    }

    public void parse(PrintWriter output, Result r, Test test) throws FileNotFoundException, JAXBException {

        output.println("<h2>" + r.getName() + "</h2>");

        output.println("<p>" + test.getDescription() + "</p>");

        output.println("<table width=100%>");
        output.println("<tr>");
        output.println("<th width=250>Observed state</th>");
        output.println("<th width=50>Occurrence</th>");
        output.println("<th width=50>Expectation</th>");
        output.println("<th>Interpretation</th>");
        output.println("<th width=50>Refs</th>");
        output.println("</tr>");

        List<State> unmatchedStates = new ArrayList<State>();
        unmatchedStates.addAll(r.getState());
        for (Case c : test.getCase()) {

            boolean matched = false;

            for (State s : r.getState()) {
                if (c.getMatch().contains(s.getId())) {
                    // match!
                    output.println("<tr bgColor=" + selectHTMLColor(c.getExpect(), s.getCount() == 0) + ">");
                    output.println("<td>" + s.getId() + "</td>");
                    output.println("<td align=center>" + s.getCount() + "</td>");
                    output.println("<td align=center>" + c.getExpect() + "</td>");
                    output.println("<td>" + c.getDescription() + "</td>");
                    output.println("<td bgColor='white'>");
                    List<Ref> list = c.getRefs();
                    for (int i = 0; i < list.size(); i++) {
                        output.println("<a href=\"" + list.get(i).getUrl() + "\">[" + (i+1) + "]</a>");
                    }
                    output.println("</td>");

                    output.println("</tr>");
                    matched = true;
                    unmatchedStates.remove(s);
                }
            }

            if (!matched) {
                for (String m : c.getMatch()) {
                    output.println("<tr bgColor=" + selectHTMLColor(c.getExpect(), true) + ">");
                    output.println("<td>" + m + "</td>");
                    output.println("<td align=center>" + 0 + "</td>");
                    output.println("<td align=center>" + c.getExpect() + "</td>");
                    output.println("<td>" + c.getDescription() + "</td>");
                    output.println("<td bgColor='white'>");
                    List<Ref> list = c.getRefs();
                    for (int i = 0; i < list.size(); i++) {
                        output.println("<a href=\"" + list.get(i).getUrl() + "\">[" + (i+1) + "]</a>");
                    }
                    output.println("</td>");
                    output.println("</tr>");
                }
            }
        }

        for (State s : unmatchedStates) {
            output.println("<tr bgColor=" + selectHTMLColor(test.getUnmatched().getExpect(), s.getCount() == 0) + ">");
            output.println("<td>" + s.getId() + "</td>");
            output.println("<td align=center>" + s.getCount() + "</td>");
            output.println("<td align=center>" + test.getUnmatched().getExpect() + "</td>");
            output.println("<td>" + test.getUnmatched().getDescription() + "</td>");
            output.println("<td bgColor='white'>");
            List<Ref> list = test.getUnmatched().getRefs();
            for (int i = 0; i < list.size(); i++) {
                output.println("<a href=\"" + list.get(i).getUrl() + "\">[" + (i+1) + "]</a>");
            }
            output.println("</td>");
            output.println("</tr>");
        }

        output.println("</table>");
    }

    public String selectHTMLColor(ExpectType type, boolean isZero) {
        String rgb = Integer.toHexString(selectColor(type, isZero).getRGB());
        return "#" + rgb.substring(2, rgb.length());
    }

    public Color selectColor(ExpectType type, boolean isZero) {
        switch (type) {
            case REQUIRED:
                return isZero ? Color.RED : Color.GREEN;
            case ACCEPTABLE:
                return isZero ? Color.LIGHT_GRAY : Color.GREEN;
            case FORBIDDEN:
                return isZero ? Color.LIGHT_GRAY : Color.RED;
            case KNOWN_ACCEPTABLE:
                return isZero ? Color.LIGHT_GRAY : Color.CYAN;
            case KNOWN_FORBIDDEN:
                return isZero ? Color.LIGHT_GRAY : Color.YELLOW;
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
