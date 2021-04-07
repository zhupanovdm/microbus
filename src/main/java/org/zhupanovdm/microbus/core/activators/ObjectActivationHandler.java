package org.zhupanovdm.microbus.core.activators;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.App;
import org.zhupanovdm.microbus.core.annotations.Activator;
import org.zhupanovdm.microbus.core.annotations.ActiveObject;
import org.zhupanovdm.microbus.core.unit.UnitQuery;
import org.zhupanovdm.microbus.core.unit.UnitUtils;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import static org.zhupanovdm.microbus.core.unit.UnitQuery.Option.EXACT_TYPE;

@Slf4j
@Activator(marker = ActiveObject.class, priority = 20)
@SuppressWarnings("unused")
public class ObjectActivationHandler implements ActivationHandler<ActiveObject> {

    @Override
    public void onDiscover(Class<?> aClass, ActiveObject metadata) {
        resolve(aClass, new UnitQuery(null, aClass, UnitUtils.name(aClass), EXACT_TYPE));
    }

    @Override
    public void onDiscover(Method method, ActiveObject annotation) {
        resolve(method, new UnitQuery(null, method.getReturnType(), UnitUtils.name(method), EXACT_TYPE));
    }

    private void resolve(Object definition, UnitQuery query) {
        Object instance = App.getContext().getUnitRegistry().request(query).orElseThrow(() -> {
            log.error("Unit metadata for {} not found with query: {}", definition, query);
            return new NoSuchElementException("Unable to find unit metadata for " + definition);
        }).resolve();

        log.debug("Active object created: {}", instance);
    }

}
