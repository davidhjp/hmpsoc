package org.systemj.config;

public class InterfaceConfig {
    public final String subSystem;
    public final String interfaceClass;
    public final String interfaceType;

    public InterfaceConfig(String subSystem, String interfaceClass, String interfaceType) {
        this.subSystem = subSystem;
        this.interfaceClass = interfaceClass;
        this.interfaceType = interfaceType;
    }

}
