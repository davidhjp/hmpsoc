package org.systemj.config;

import java.time.Clock;
import java.util.*;

public class SubSystemConfig {
    public static String HMPSOC_DEVICE_TYPE = "hmpsoc";

    private SystemConfig systemConfig;

    public final String name;
    public final boolean local;
    public final String device;
    public final Map<String, ClockDomainConfig> clockDomains;

    public SubSystemConfig(String name, boolean local, String device, List<ClockDomainConfig> clockDomains) {
        this.name = name;
        this.local = local;
        this.device = device;

        Map<String, ClockDomainConfig> mapClockDomains = new HashMap<>();
        for (ClockDomainConfig cd : clockDomains) {
            mapClockDomains.put(cd.className, cd);
        }
        this.clockDomains = Collections.unmodifiableMap(mapClockDomains);
    }

    public void setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    public boolean isHmpsocSystem() {
        return HMPSOC_DEVICE_TYPE.equalsIgnoreCase(device);
    }

    public void validate() {
        for (ClockDomainConfig cd : clockDomains.values()) {
            cd.setSystemConfig(systemConfig);
        }

        if (!local)
            // We don't really care if the config is invalid for a non-local subsystem
            return;

//        Set<Integer> usedISignalIndexes = new HashSet<>();
//        Set<Integer> usedOSignalIndexes = new HashSet<>();
        for (ClockDomainConfig cd : clockDomains.values()) {
            cd.validate();
//            validateSignalIndexes(usedISignalIndexes, cd.isignalIndexes, cd.name);
//            validateSignalIndexes(usedOSignalIndexes, cd.osignalIndexes, cd.name);
        }
    }

//    private void validateSignalIndexes(Set<Integer> usedSignalIndexes, Map<String, Integer> signalIndexes, String cdName) {
//        for (Map.Entry<String, Integer> e : signalIndexes.entrySet()) {
//            if (!usedSignalIndexes.add(e.getValue()))
//                throw new RuntimeException("Signal " + e.getKey() + "s index is used by another signal outside of ClockDomain " + cdName);
//        }
//    }
}
