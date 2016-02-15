package org.systemj.config;

import java.util.Collections;
import java.util.Map;

public class ClockDomainConfig {
    private SystemConfig systemConfig;

    public final String name;
    public final String className;
    public final Map<String, SignalConfig> isignals;
    public final Map<String, SignalConfig> osignals;
    public final Map<String, String> channelPartners;

    public ClockDomainConfig(String name, String className, Map<String, SignalConfig> isignals, Map<String, SignalConfig> osignals, Map<String, String> channelPartners) {
        this.name = name;
        this.className = className;
        this.isignals = Collections.unmodifiableMap(isignals);
        this.osignals = Collections.unmodifiableMap(osignals);
        this.channelPartners = Collections.unmodifiableMap(channelPartners);
    }

    public void setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    public void validate() {
//        validateSignalIndexes(isignals);
//        validateSignalIndexes(osignals);
    }

//    private void validateSignalIndexes(Map<String, SignalConfig> signalIndexes) {
//        Set<Integer> usedSignalIndexes = new HashSet<>();
//        for (Map.Entry<String, SignalConfig> e : signalIndexes.entrySet()) {
//            if (!usedSignalIndexes.add(e.getValue()))
//                throw new RuntimeException("Signal " + e.getKey() + "s index is used by another signal within ClockDomain " + name);
//        }
//    }

    public boolean isChannelPartnerLocal(String name) {
        String channelPartner = channelPartners.get(name);

        if (channelPartner == null) throw new RuntimeException("No configuration for channel " + name + " in ClockDomain " + this.name);

        String[] channelPartnerInfo = channelPartner.split("\\.");
        String cdName = channelPartnerInfo[0];

        return systemConfig.isLocalClockDomain(cdName);
    }
}
