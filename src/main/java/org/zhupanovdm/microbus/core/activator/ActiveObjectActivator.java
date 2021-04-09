package org.zhupanovdm.microbus.core.activator;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.annotation.ActiveObject;
import org.zhupanovdm.microbus.core.di.UnitHolder;
import org.zhupanovdm.microbus.core.di.UnitQuery;
import org.zhupanovdm.microbus.core.di.UnitUtils;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.EXACT_TYPE;
import static org.zhupanovdm.microbus.core.di.UnitUtils.nameOf;

@Slf4j
@Activator(marker = ActiveObject.class, priority = 20)
@SuppressWarnings("unused")
public class ActiveObjectActivator extends AbstractActivator<ActiveObject> {

    @Override
    public void onDiscover(Class<?> aClass, ActiveObject metadata) {
        resolve(aClass, new UnitQuery(null, aClass, nameOf(aClass), EXACT_TYPE));
    }

    @Override
    public void onDiscover(Method method, ActiveObject annotation) {
        resolve(method, new UnitQuery(null, method.getReturnType(), nameOf(method), EXACT_TYPE));
    }

    private void resolve(Object definition, UnitQuery query) {
        UnitHolder unit = context.getUnitRegistry().request(query).orElseThrow(() -> {
            log.error("Unit metadata defined with {} not found", definition);
            return new NoSuchElementException("Unit metadata not found " + query);
        });
        Object instance = unit.resolve();

        log.debug("{} instance created: {}", unit, instance);
    }

}
