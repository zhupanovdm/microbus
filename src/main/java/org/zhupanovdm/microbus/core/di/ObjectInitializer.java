package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.AppContext;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.Field;
import java.util.function.Function;

@Slf4j
@NotThreadSafe
public class ObjectInitializer {
    private final AppContext context;

    public ObjectInitializer(AppContext context) {
        this.context = context;
    }

    public void init(Object instance, Function<UnitQuery, ?> injector) {
        DependencyQualifier<Field> qualifier = context.getFieldQualifier();
        qualifier.getAll().stream()
            .filter(field -> field.getDeclaringClass().equals(instance.getClass()))
            .forEach(field -> inject(instance, field, injector.apply(qualifier.toQuery(field))));
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
