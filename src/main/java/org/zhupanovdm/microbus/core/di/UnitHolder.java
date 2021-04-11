package org.zhupanovdm.microbus.core.di;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(of = { "id" })
@Data
public class UnitHolder {
    private final String id;
    private final String name;
    private final Class<?> type;

    private Object instance;
    private Class<? extends CreationStrategy> creationStrategy;

    private final InjectableExecutable<?> constructor;

    public UnitHolder(String id, Class<?> type, InjectableExecutable<?> constructor, String name, Class<? extends CreationStrategy> creationStrategy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.constructor = constructor;
        this.creationStrategy = creationStrategy;
    }

    public String toString() {
        return "UNIT@" + id;
    }
}