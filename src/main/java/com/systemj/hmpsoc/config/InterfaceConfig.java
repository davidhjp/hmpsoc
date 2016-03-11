package com.systemj.hmpsoc.config;

import java.util.Collections;
import java.util.Map;

public class InterfaceConfig {
    public final String subSystem;
    public final String interfaceClass;
    public final Map<String, String> cfg;

    public InterfaceConfig(String subSystem, String interfaceClass, Map<String, String> cfg) {
        this.subSystem = subSystem;
        this.interfaceClass = interfaceClass;
        this.cfg = Collections.unmodifiableMap(cfg);
    }

}
