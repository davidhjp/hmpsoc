package org.systemj.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkConfig {
    public final String type;
    public final List<InterfaceConfig> interfaces;

    public LinkConfig(String type, List<InterfaceConfig> interfaces) {
        this.type = type;
        this.interfaces = Collections.unmodifiableList(interfaces);
    }
}
