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

public class Options {
    private String resultFile;
    private String testRegexp;
    private int loops;
    private int time;
    private final String[] args;

    public Options(String[] args) {
        this.args = args;
    }

    public boolean parse() throws IOException {
        OptionParser parser = new OptionParser();
        parser.formatHelpWith(new OptFormatter());

        OptionSpec<String> result = parser.accepts("r", "Results file")
                .withRequiredArg().ofType(String.class).describedAs("file").defaultsTo("results.xml");

        OptionSpec<String> testRegexp = parser.accepts("t", "Regexp selector for tests")
                .withRequiredArg().ofType(String.class).describedAs("file").defaultsTo(".*");

        OptionSpec<Integer> loops = parser.accepts("loops", "Number of internal loops")
                .withRequiredArg().ofType(int.class).describedAs("N").defaultsTo(10);

        OptionSpec<Integer> time = parser.accepts("time", "Time per test")
                .withRequiredArg().ofType(int.class).describedAs("ms").defaultsTo(1000);

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
        this.testRegexp = set.valueOf(testRegexp);
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
}
