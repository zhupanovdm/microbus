package org.zhupanovdm.microbus.core.activator;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.annotations.Activator;
import org.zhupanovdm.microbus.core.annotations.ActiveObject;
import org.zhupanovdm.microbus.core.unit.UnitHolder;
import org.zhupanovdm.microbus.core.unit.UnitQuery;
import org.zhupanovdm.microbus.core.unit.UnitUtils;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import static org.zhupanovdm.microbus.core.unit.UnitQuery.Option.EXACT_TYPE;

@Slf4j
@Activator(marker = ActiveObject.class, priority = 20)
@SuppressWarnings("unused")
public class ActiveObjectActivator extends AbstractActivator<ActiveObject> {

    @Override
    public void onDiscover(Class<?> aClass, ActiveObject metadata) {
        resolve(aClass, new UnitQuery(null, aClass, UnitUtils.nameOf(aClass), EXACT_TYPE));
    }

    @Override
    public void onDiscover(Method method, ActiveObject annotation) {
        resolve(method, new UnitQuery(null, method.getReturnType(), UnitUtils.nameOf(method), EXACT_TYPE));
    }

    private void resolve(Object definition, UnitQuery query) {
        UnitHolder<?> unit = context.getUnitRegistry().request(query).orElseThrow(() -> {
            log.error("Unit metadata defined with {} not found", definition);
            return new NoSuchElementException("Unit metadata not found " + query);
        });
        Object instance = unit.resolve();

        log.debug("{} instance created: {}", unit, instance);
    }

}
