package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Slf4j
@ThreadSafe
public class DefaultDependencyQualifier<T> implements DependencyQualifier<T> {
    private final Map<T, UnitQuery> defined = new ConcurrentHashMap<>();
    private final AtomicReference<Function<T, UnitQuery>> defaultMapper;

    public DefaultDependencyQualifier(Function<T, UnitQuery> defaultMapper) {
        this.defaultMapper = new AtomicReference<>(defaultMapper);
    }

    public DefaultDependencyQualifier() {
        this(null);
    }

    @Override
    public void setDefault(Function<T, UnitQuery> mapper) {
        this.defaultMapper.set(mapper);
    }

    @Override
    public void define(T dependent, UnitQuery query) {
        defined.put(dependent, query);
        log.trace("Qualifier defined: {} => {}", dependent, query);
    }

    @Override
    public UnitQuery qualify(T element) {
        UnitQuery query = defined.get(element);
        Function<T, UnitQuery> mapper = defaultMapper.get();
        if (query == null && mapper != null) {
            query = mapper.apply(element);
            log.trace("Qualified (default) {} with: {}", element, query);
            return query;
        }
        log.trace("Qualified {} with: {}", element, query);
        return query;
    }

    @Override
    public Set<T> getAll() {
        return defined.keySet();
    }
}
