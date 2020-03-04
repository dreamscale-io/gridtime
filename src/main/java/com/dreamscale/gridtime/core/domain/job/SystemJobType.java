package com.dreamscale.gridtime.core.domain.job;

import com.dreamscale.gridtime.core.machine.executor.job.CalendarJobDescriptor;

public enum SystemJobType {

    CALENDAR_TILE_GENERATOR(CalendarJobDescriptor.class);

    private final Class<?> descriptorClazz;

    SystemJobType(Class<?> descriptorClazz) {
        this.descriptorClazz = descriptorClazz;
    }

    public Class<?> getDescriptorClazz() {
        return descriptorClazz;
    }
}
