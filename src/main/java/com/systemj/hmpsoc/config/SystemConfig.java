package com.systemj.hmpsoc.config;

import java.util.Collections;
import java.util.List;

public class SystemConfig {
    public final List<LinkConfig> links;
    public final List<SubSystemConfig> subSystems;

    public SystemConfig(List<LinkConfig> links, List<SubSystemConfig> subSystems) {
        this.links = Collections.unmodifiableList(links);
        this.subSystems = Collections.unmodifiableList(subSystems);
    }

    public ClockDomainConfig getClockDomain(String name) {
        for (SubSystemConfig ss : subSystems) {
            ClockDomainConfig cd = ss.clockDomains.get(name);
            if (cd != null) return cd;
        }
        return null;
    }

    public void validate() {
        for (SubSystemConfig ss : subSystems) {
            ss.setSystemConfig(this);
            ss.validate();
        }
    }

    public boolean isLocalClockDomain(String cdName) {
        for (SubSystemConfig ss : subSystems) {
            ClockDomainConfig cd = ss.clockDomains.get(cdName);
            if (cd != null) return ss.local;
        }
        return false;
    }
}
