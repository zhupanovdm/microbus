package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class DefaultDependencyQualifier<T> implements DependencyQualifier<T> {
    private final Map<T, UnitQuery> defined = new ConcurrentHashMap<>();
    private Function<T, UnitQuery> defaultMapper;

    public DefaultDependencyQualifier(Function<T, UnitQuery> defaultMapper) {
        this.defaultMapper = defaultMapper;
    }

    public DefaultDependencyQualifier() {
        this(null);
    }

    @Override
    public void setDefault(Function<T, UnitQuery> mapper) {
        this.defaultMapper = mapper;
    }

    @Override
    public void define(T dependent, UnitQuery query) {
        defined.put(dependent, query);
    }

    @Override
    public UnitQuery qualify(T element) {
        UnitQuery query = defined.get(element);
        if (query == null && defaultMapper != null)
            return defaultMapper.apply(element);
        return query;
    }

    @Override
    public Set<T> getAll() {
        return defined.keySet();
    }
}
