package org.zhupanovdm.microbus.core.activator;

import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.annotation.Inject;
import org.zhupanovdm.microbus.core.di.UnitQuery;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@Activator(marker = Inject.class, priority = 11)
@SuppressWarnings("unused")
public class InjectActivator extends AbstractActivator<Inject> {

    @Override
    public void onDiscover(Field field, Inject metadata) {
        context.getFieldQualifier().qualify(field, metadata.value().isBlank() ? UnitQuery.of(field) :
                        new UnitQuery(metadata.value(), field.getType(), null));
    }

    @Override
    public void onDiscover(Parameter arg, Inject metadata) {
        context.getArgumentQualifier().qualify(arg, metadata.value().isBlank() ? UnitQuery.of(arg) :
                new UnitQuery(metadata.value(), arg.getType(), null));
    }

}
