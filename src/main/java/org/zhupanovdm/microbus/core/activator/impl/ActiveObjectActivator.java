package org.zhupanovdm.microbus.core.activator.impl;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.activator.ActivatorTemplate;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.annotation.ActiveObject;
import org.zhupanovdm.microbus.core.di.InstanceProvider;
import org.zhupanovdm.microbus.core.di.UnitQuery;

import java.lang.reflect.Method;

import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.EXACT_TYPE;
import static org.zhupanovdm.microbus.core.di.UnitUtils.nameOf;

@Slf4j
@Activator(marker = ActiveObject.class, priority = 20)
@SuppressWarnings("unused")
public class ActiveObjectActivator implements ActivatorTemplate<ActiveObject> {
    private final InstanceProvider provider;

    public ActiveObjectActivator() {
        this.provider = App.getContext().getInstanceProvider();
    }

    @Override
    public void onDiscover(Class<?> aClass, ActiveObject metadata) {
        String name = nameOf(aClass);
        Object instance = provider.resolve(new UnitQuery(null, aClass, name, EXACT_TYPE));
        log.debug("Active object resolved: {} from definition: {}", instance, name);
    }

    @Override
    public void onDiscover(Method method, ActiveObject annotation) {
        String name = nameOf(method);
        Object instance = provider.resolve(new UnitQuery(null, method.getReturnType(), name, EXACT_TYPE));
        log.debug("Active object resolved: {} from definition: {}", instance, name);
    }

}
