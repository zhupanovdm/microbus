package org.zhupanovdm.microbus.core.activator;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.annotation.Unit;
import org.zhupanovdm.microbus.core.di.UnitHolder.Constructable;
import org.zhupanovdm.microbus.core.di.UnitHolder.Invokable;

import java.lang.reflect.Method;

import static org.zhupanovdm.microbus.core.di.UnitUtils.idOf;

@Slf4j
@Activator(marker = Unit.class, priority = 10)
@SuppressWarnings("unused")
public class UnitActivator extends AbstractActivator<Unit> {

    @Override
    public void onDiscover(Class<?> clazz, Unit metadata) {
        context.getUnitRegistry().register(new Constructable(idOf(clazz), clazz, metadata.strategy()));
    }

    @Override
    public void onDiscover(Method method, Unit metadata) {
        context.getUnitRegistry().register(new Invokable(idOf(method), method, metadata.strategy()));
    }

}