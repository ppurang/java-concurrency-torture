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

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.util.regex.Pattern;

public class Options {
    private String resultFile;
    private Pattern testRegexp;
    private int loops;
    private int time;
    private int wtime;
    private int witers;
    private final String[] args;
    private boolean shouldYield;
    private boolean parse;

    public Options(String[] args) {
        this.args = args;
    }

    public boolean parse() throws IOException {
        OptionParser parser = new OptionParser();
        parser.formatHelpWith(new OptFormatter());

        OptionSpec<String> result = parser.accepts("r", "Results file")
                .withRequiredArg().ofType(String.class).describedAs("file").defaultsTo("results.xml");

        OptionSpec<Boolean> parse = parser.accepts("p", "Run parser on the result file")
                .withOptionalArg().ofType(boolean.class).defaultsTo(false);

        OptionSpec<String> testRegexp = parser.accepts("t", "Regexp selector for tests")
                .withRequiredArg().ofType(String.class).describedAs("file").defaultsTo(".*");

        OptionSpec<Integer> loops = parser.accepts("loops", "Number of internal loops")
                .withRequiredArg().ofType(int.class).describedAs("N").defaultsTo(10);

        OptionSpec<Integer> time = parser.accepts("time", "Time per test")
                .withRequiredArg().ofType(int.class).describedAs("ms").defaultsTo(1000);

        OptionSpec<Integer> wtime = parser.accepts("wtime", "Warmup time per test")
                .withRequiredArg().ofType(int.class).describedAs("ms").defaultsTo(1000);

        OptionSpec<Integer> witers = parser.accepts("witers", "Warmup iterations per test")
                .withRequiredArg().ofType(int.class).describedAs("N").defaultsTo(5);

        OptionSpec<Boolean> shouldYield = parser.accepts("yield", "Make yields in busyloops")
                .withOptionalArg().ofType(boolean.class).defaultsTo(false);

        parser.accepts("h", "Print this help");

        OptionSet set;
        try {
            set = parser.parse(args);
        } catch (OptionException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.err.println();
            parser.printHelpOn(System.err);
            return false;
        }

        if (set.has("h")) {
            parser.printHelpOn(System.out);
            return false;
        }

        this.resultFile = set.valueOf(result);
        this.loops = set.valueOf(loops);
        this.time = set.valueOf(time);
        this.wtime = set.valueOf(wtime);
        this.witers = set.valueOf(witers);
        this.testRegexp = Pattern.compile(set.valueOf(testRegexp));
        this.shouldYield = set.has(shouldYield);
        this.parse = set.has(parse);
        return true;
    }

    public int getLoops() {
        return loops;
    }

    public String getResultFile() {
        return resultFile;
    }

    public int getTime() {
        return time;
    }

    public int getWarmupTime() {
        return wtime;
    }

    public int getWarmupIterations() {
        return witers;
    }

    public boolean shouldYield() {
        return shouldYield;
    }

    public boolean shouldParse() {
        return parse;
    }

    public Pattern getTestRegexp() {
        return testRegexp;
    }
}
