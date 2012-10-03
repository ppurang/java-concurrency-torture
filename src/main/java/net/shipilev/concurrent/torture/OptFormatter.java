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

import joptsimple.HelpFormatter;
import joptsimple.OptionDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OptFormatter implements HelpFormatter {

    public String format( Map<String, ? extends OptionDescriptor> options ) {
        StringBuilder buffer = new StringBuilder();

        Collection<OptionDescriptor> required = new ArrayList<OptionDescriptor>();
        Collection<OptionDescriptor> optional = new ArrayList<OptionDescriptor>();
        for (OptionDescriptor each : options.values()) {
            if (each.isRequired()) {
                required.add(each);
            } else {
                optional.add(each);
            }
        }

        if (!required.isEmpty()) {
            buffer.append("Mandatory options are:\n");
            for (OptionDescriptor each : required) {
                buffer.append(lineFor(each));
            }
            buffer.append("\n");
        }

        if (!optional.isEmpty()) {
            buffer.append("Non-mandatory options are:\n");
            for (OptionDescriptor each : optional) {
                buffer.append(lineFor(each));
            }
        }

        return buffer.toString();
    }

    private String lineFor( OptionDescriptor descriptor ) {
        StringBuilder line = new StringBuilder();

        StringBuilder optionList = new StringBuilder();
        optionList.append("  ");
        for(String str : descriptor.options()) {
            optionList.append("-").append(str);
            if (descriptor.acceptsArguments()) {
                optionList.append(" <").append(descriptor.argumentDescription()).append(">");
              }
        }

        line.append(String.format("%-30s", optionList.toString()));
        String[] dLines = descriptor.description().split("\n");
        line.append(" ").append(dLines[0]);
        for (int c = 1; c < dLines.length; c++) {
            line.append("\n");
            line.append(String.format("%-35s", "")).append(dLines[c]);
        }

        List<?> defValues = descriptor.defaultValues();
        if (defValues.size() > 0) {
            line.append( " (default: " );
            if (defValues.size() > 1) {
                line.append(defValues);
            } else {
                line.append(defValues.get(0));
            }
            line.append(")");
        }

        line.append( System.getProperty( "line.separator" ) );
        return line.toString();
    }

}
