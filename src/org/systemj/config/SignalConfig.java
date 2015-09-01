package org.systemj.config;

import java.util.Map;

public class SignalConfig {
    public final String name;
    public final String clazz;
    public final Map<String, String> cfg;

    public SignalConfig(String name, String clazz, Map<String, String> cfg) {
        this.name = name;
        this.clazz = clazz;
        this.cfg = cfg;
    }
}
