package org.zhupanovdm.microbus.core.components;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.unit.UnitQuery;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class DependencyQualifier<T> {
    private final Map<T, UnitQuery> definedDependencies = new ConcurrentHashMap<>();
    private final Function<T, UnitQuery> defaultDependency;

    public DependencyQualifier(Function<T, UnitQuery> defaultDependency) {
        this.defaultDependency = defaultDependency;
    }

    public void qualify(T element, UnitQuery query) {
        definedDependencies.put(element, query);
        log.trace("Qualified {} with {}", element, query);
    }

    public UnitQuery toQuery(T element) {
        UnitQuery predefined = definedDependencies.get(element);
        UnitQuery query = predefined == null ? defaultDependency.apply(element) : predefined;
        log.trace("Mapped {} to {}", element, query);
        return query;
    }

    public Set<T> getAll() {
        return definedDependencies.keySet();
    }
}
