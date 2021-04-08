package org.zhupanovdm.microbus.core.components;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.unit.UnitQuery;
import org.zhupanovdm.microbus.core.unit.UnitRegistry;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;

@Slf4j
@NotThreadSafe
public class ObjectInitializer {
    private final UnitRegistry registry;
    private final DependencyQualifier<Field> qualifier;

    public ObjectInitializer(UnitRegistry registry, DependencyQualifier<Field> qualifier) {
        this.registry = registry;
        this.qualifier = qualifier;
    }

    public void init(Object instance) {
        qualifier.getAll().stream()
            .filter(field -> field.getDeclaringClass().equals(instance.getClass()))
            .forEach(field -> {
                UnitQuery query = qualifier.toQuery(field);
                inject(instance, field, registry.request(query).orElseThrow(() -> {
                    log.error("Cannot satisfy injection to {} with query {} for instance {}", field, query, instance);
                    return new NoSuchElementException("Appropriate unit not found " + query);
                }).resolve());
            });
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
