package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@ThreadSafe
public class DependencyQualifier<T> {
    private final Map<T, UnitQuery> definedDependencies = new ConcurrentHashMap<>();
    private final Function<T, UnitQuery> defaultDependency;

    public DependencyQualifier(Function<T, UnitQuery> defaultDependency) {
        this.defaultDependency = defaultDependency;
    }

    public DependencyQualifier() {
        this(null);
    }

    public void qualify(T element, UnitQuery query) {
        definedDependencies.put(element, query);
    }

    public UnitQuery toQuery(T element) {
        UnitQuery query = definedDependencies.get(element);
        if (query == null && defaultDependency != null)
            query = defaultDependency.apply(element);
        return query;
    }

    public Set<T> getAll() {
        return definedDependencies.keySet();
    }
}
