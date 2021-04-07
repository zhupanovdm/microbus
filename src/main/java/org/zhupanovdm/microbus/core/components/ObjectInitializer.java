package org.zhupanovdm.microbus.core.components;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.unit.UnitQuery;
import org.zhupanovdm.microbus.core.unit.UnitRegistry;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

@Slf4j
public class ObjectInitializer {
    private final UnitRegistry registry;
    private final DependencyQualifier<Field> qualifier;

    public ObjectInitializer(UnitRegistry registry, DependencyQualifier<Field> qualifier) {
        this.registry = registry;
        this.qualifier = qualifier;
    }

    public Object init(Object instance) {
        log.trace("Initializing {}", instance);

        qualifier.getAll().stream()
            .filter(field -> field.getDeclaringClass().equals(instance.getClass()))
            .forEach(field -> {
                UnitQuery query = qualifier.toQuery(field);
                inject(instance, field, registry.request(query).orElseThrow(() -> {
                    log.error("Cannot satisfy injection query {} for instance {}", query, instance);
                    return new NoSuchElementException("Appropriate unit not found");
                }).resolve());
            });
        return instance;
    }

    private void inject(Object instance, Field field, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            inject(instance, field, value);
            field.setAccessible(false);
        }
        log.trace("Injection to {} of {}", field, value);
    }

}
