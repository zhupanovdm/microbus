package org.zhupanovdm.microbus.core.activators;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.App;
import org.zhupanovdm.microbus.core.annotations.Activator;
import org.zhupanovdm.microbus.core.annotations.Inject;
import org.zhupanovdm.microbus.core.unit.UnitQuery;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@Slf4j
@Activator(marker = Inject.class, priority = 11)
@SuppressWarnings("unused")
public class InjectRegistrar implements ActivationHandler<Inject> {

    @Override
    public void onDiscover(Field field, Inject metadata) {
        App.getContext().getFieldQualifier().qualify(field, metadata.value().isBlank() ? UnitQuery.of(field) :
                        new UnitQuery(metadata.value(), field.getType(), null));
    }

    @Override
    public void onDiscover(Parameter arg, Inject metadata) {
        App.getContext().getArgumentQualifier().qualify(arg, metadata.value().isBlank() ? UnitQuery.of(arg) :
                new UnitQuery(metadata.value(), arg.getType(), null));
    }

}
