package net.shipilev.concurrent.torture.util;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    public static Map<String, String> getEnvironment() {
        Map<String, String> result = new HashMap<String, String>();

        String[] keys = new String[] {
                "java.version",
                "java.vendor",
                "java.vm.version",
                "java.vm.vendor",
                "java.vm.name",
                "java.specification.version",
                "java.specification.vendor",
                "java.specification.name",
                "os.name",
                "os.arch",
                "os.version"
        };

        for (String key : keys) {
            result.put(key, System.getProperty(key));
        }

        return result;
    }

}
