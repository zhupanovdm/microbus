package org.zhupanovdm.microbus.core.activators;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.App;
import org.zhupanovdm.microbus.core.annotations.Activator;
import org.zhupanovdm.microbus.core.annotations.Unit;
import org.zhupanovdm.microbus.core.unit.UnitHolder;

import java.lang.reflect.Method;

import static org.zhupanovdm.microbus.core.unit.UnitUtils.definedId;

@Slf4j
@Activator(marker = Unit.class, priority = 10)
@SuppressWarnings("unused")
public class UnitRegistrar implements ActivationHandler<Unit> {

    @Override
    public void onDiscover(Class<?> clazz, Unit metadata) {
        App.getContext().getUnitRegistry().register(new UnitHolder.Constructable(definedId(clazz), clazz, metadata.strategy()));
    }

    @Override
    public void onDiscover(Method method, Unit metadata) {
        App.getContext().getUnitRegistry().register(new UnitHolder.Invokable(definedId(method), method, metadata.strategy()));
    }

}