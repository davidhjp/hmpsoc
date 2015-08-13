package org.systemj.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClockDomainConfig {
    private SystemConfig systemConfig;

    public final String name;
    public final String className;
    public final Map<String, Integer> isignalIndexes;
    public final Map<String, Integer> osignalIndexes;
    public final Map<String, String> channelPartners;

    public ClockDomainConfig(String name, String className, Map<String, Integer> isignalIndexes, Map<String, Integer> osignalIndexes, Map<String, String> channelPartners) {
        this.name = name;
        this.className = className;
        this.isignalIndexes = Collections.unmodifiableMap(isignalIndexes);
        this.osignalIndexes = Collections.unmodifiableMap(osignalIndexes);
        this.channelPartners = Collections.unmodifiableMap(channelPartners);
    }

    public void setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    public void validate() {
        validateSignalIndexes(isignalIndexes);
        validateSignalIndexes(osignalIndexes);
    }

    private void validateSignalIndexes(Map<String, Integer> signalIndexes) {
        Set<Integer> usedSignalIndexes = new HashSet<>();
        for (Map.Entry<String, Integer> e : signalIndexes.entrySet()) {
            if (!usedSignalIndexes.add(e.getValue()))
                throw new RuntimeException("Signal " + e.getKey() + "s index is used by another signal within ClockDomain " + name);
        }
    }

    public boolean isChannelPartnerLocal(String name) {
        String channelPartner = channelPartners.get(name);

        if (channelPartner == null) throw new RuntimeException("No configuration for channel " + name + " in ClockDomain " + this.name);

        String[] channelPartnerInfo = channelPartner.split("\\.");
        String cdName = channelPartnerInfo[0];

        try {
            return systemConfig.isLocalClockDomain(cdName);
        } catch (RuntimeException e) {
            throw new RuntimeException("The channel " + name + " in ClockDomain " + this.name + " has a partner with an non-existing ClockDomain " + cdName, e);
        }
    }
}
