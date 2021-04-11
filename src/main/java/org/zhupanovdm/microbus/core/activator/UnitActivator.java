package org.zhupanovdm.microbus.core.activator;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.annotation.Unit;
import org.zhupanovdm.microbus.core.di.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.EXACT_TYPE;
import static org.zhupanovdm.microbus.core.di.UnitUtils.idOf;
import static org.zhupanovdm.microbus.core.di.UnitUtils.nameOf;

@Slf4j
@Activator(marker = Unit.class, priority = 10)
@SuppressWarnings("unused")
public class UnitActivator implements ActivatorTemplate<Unit> {
    private final UnitRegistry registry;
    private final DependencyQualifierProvider provider;

    public UnitActivator() {
        registry = App.getContext().getUnitRegistry();
        provider = App.getContext().getQualifierProvider();
    }

    @Override
    public void onDiscover(Class<?> clazz, Unit metadata) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length != 1) {
            log.error("Only one constructor is supported for units {}", clazz);
            throw new IllegalArgumentException("Only one constructor is supported: " + clazz);
        }

        InjectableConstructor constructor = new InjectableConstructor(constructors[0], provider);
        registry.register(new UnitHolder(idOf(clazz), clazz, constructor, nameOf(clazz), metadata.strategy()));
    }

    @Override
    public void onDiscover(Method method, Unit metadata) {
        provider.define(method, new UnitQuery(null, method.getDeclaringClass(), nameOf(method.getDeclaringClass()), EXACT_TYPE));

        InjectableMethod constructor = new InjectableMethod(method, provider);
        registry.register(new UnitHolder(idOf(method), method.getReturnType(), constructor, nameOf(method), metadata.strategy()));
    }

}