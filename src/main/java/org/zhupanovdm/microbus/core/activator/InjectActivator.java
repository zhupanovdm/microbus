package org.zhupanovdm.microbus.core.activator;

import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.annotation.Inject;
import org.zhupanovdm.microbus.core.di.DependencyQualifierProvider;
import org.zhupanovdm.microbus.core.di.UnitQuery;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import static org.zhupanovdm.microbus.util.CommonUtils.isDefined;

@Activator(marker = Inject.class, priority = 11)
@SuppressWarnings("unused")
public class InjectActivator implements ActivatorTemplate<Inject> {
    private final DependencyQualifierProvider provider;

    public InjectActivator() {
        provider = App.getContext().getQualifierProvider();
    }

    @Override
    public void onDiscover(Field field, Inject metadata) {
        if (isDefined(metadata.value()))
            provider.define(field, new UnitQuery(metadata.value(), field.getType(), null));
    }

    @Override
    public void onDiscover(Parameter arg, Inject metadata) {
        if (isDefined(metadata.value()))
            provider.define(arg, new UnitQuery(metadata.value(), arg.getType(), null));
    }

}
