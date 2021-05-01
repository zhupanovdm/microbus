package org.zhupanovdm.microbus.core.activator.impl;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.activator.ActivatorTemplate;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.annotation.Unit;
import org.zhupanovdm.microbus.core.di.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.EXACT_TYPE;
import static org.zhupanovdm.microbus.core.di.UnitUtils.idOf;
import static org.zhupanovdm.microbus.core.di.UnitUtils.nameOf;

@Slf4j
@Activator(marker = Unit.class, priority = 10)
public class UnitActivator implements ActivatorTemplate<Unit> {
    private final UnitRegistry registry;
    private final DependencyQualifierProvider qualifier;

    public UnitActivator() {
        registry = App.getContext().getUnitRegistry();
        qualifier = App.getContext().getQualifierProvider();
    }

    @Override
    public void onDiscover(Class<?> clazz, Unit metadata) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 0) {
            log.error("Only constructable types are supported for unit creation. Constructor not found for type: {}", clazz);
            throw new NoSuchElementException("Constructor not found: " + clazz);
        }
        if (constructors.length > 1) {
            log.error("Ambiguous constructors definition: which one to choose for unit construction? Several constructors found: {}", clazz);
            throw new IllegalArgumentException("Ambiguous constructors definition: " + clazz);
        }

        InjectableConstructor constructor = new InjectableConstructor(constructors[0], qualifier);
        UnitHolder unit = new UnitHolder(idOf(clazz), clazz, constructor, nameOf(clazz), metadata.strategy());
        registry.register(unit);
    }

    @Override
    public void onDiscover(Method method, Unit metadata) {
        qualifier.define(method,
                UnitQuery.create()
                    .type(method.getDeclaringClass())
                    .name(nameOf(method.getDeclaringClass()))
                    .options(EXACT_TYPE)
                    .build()
        );

        InjectableMethod constructor = new InjectableMethod(method, qualifier);
        UnitHolder unit = new UnitHolder(idOf(method), method.getReturnType(), constructor, nameOf(method), metadata.strategy());
        registry.register(unit);
    }

}