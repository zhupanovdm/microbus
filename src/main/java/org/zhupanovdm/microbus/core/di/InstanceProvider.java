package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class InstanceProvider {
    private final UnitRegistry registry;
    private final DependencyQualifierProvider provider;

    private final Map<Class<? extends CreationStrategy>, CreationStrategy> strategies = new HashMap<>();

    public InstanceProvider(UnitRegistry registry, DependencyQualifierProvider provider) {
        this.registry = registry;
        this.provider = provider;
    }

    public void registerCreationStrategy(CreationStrategy strategy) {
        strategies.put(strategy.getClass(), strategy);
    }

    public Object resolve(UnitHolder unit) {
        return resolve(unit, new LinkedHashSet<>());
    }

    public Object resolve(UnitQuery query) {
        return resolve(query, new LinkedHashSet<>());
    }

    private Object resolve(UnitHolder unit, Set<UnitHolder> chain) {
        if (!chain.add(unit)) {
            log.error("Can not resolve recursive dependency for {} previous resolves: {}", unit, chain);
            throw new IllegalStateException("Can not resolve recursive dependency");
        }

        CreationStrategy strategy = strategies.get(unit.getCreationStrategy());
        if (strategy == null) {
            log.error("Unknown creation strategy for {}: {}", unit, unit.getCreationStrategy());
            throw new IllegalStateException("Unknown creation strategy");
        }

        Function<UnitQuery, ?> injector = query -> resolve(query, chain);
        Object instance = strategy.getInstance(unit, () -> {
            Object obj = unit.getConstructor().invoke(injector);
            init(obj, injector);
            return obj;
        });
        chain.remove(unit);

        return instance;
    }

    private Object resolve(UnitQuery query, Set<UnitHolder> chain) {
        return resolve(registry.request(query).orElseThrow(() -> {
            log.error("Failed to satisfy unit dependency with query {}", query);
            return new NoSuchElementException("No unit found on specified request");
        }), chain);
    }

    private void init(Object instance, Function<UnitQuery, ?> injector) {
        provider.getFields().getAll().stream()
            .filter(field -> field.getDeclaringClass().equals(instance.getClass()))
            .forEach(field -> inject(instance, field, injector.apply(provider.qualify(field))));
    }

    private void inject(Object instance, Field field, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            inject(instance, field, value);
            field.setAccessible(false);
        }
    }

}
