package org.systemj.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkConfig {
    public final String type;
    public final Map<String, InterfaceConfig> interfaces;

    public LinkConfig(String type, List<InterfaceConfig> interfaces) {
        this.type = type;

        Map<String, InterfaceConfig> mapInterfaces = new HashMap<>();
        for (InterfaceConfig i : interfaces) {
            mapInterfaces.put(i.subSystem, i);
        }
        this.interfaces = Collections.unmodifiableMap(mapInterfaces);
    }
}
