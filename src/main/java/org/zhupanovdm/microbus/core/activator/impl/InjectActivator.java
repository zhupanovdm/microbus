package org.zhupanovdm.microbus.core.activator.impl;

import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.activator.ActivatorTemplate;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.annotation.Inject;
import org.zhupanovdm.microbus.core.di.DependencyQualifierProvider;
import org.zhupanovdm.microbus.core.di.UnitQuery;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import static org.zhupanovdm.microbus.CommonUtils.isDefined;

@Activator(marker = Inject.class, priority = 11)
@SuppressWarnings("unused")
public class InjectActivator implements ActivatorTemplate<Inject> {
    private final DependencyQualifierProvider provider;

    public InjectActivator() {
        provider = App.getContext().getQualifierProvider();
    }

    @Override
    public void onDiscover(Field field, Inject metadata) {
        UnitQuery query = new UnitQuery(isDefined(metadata.value()) ? metadata.value() : null, field.getType(), null);
        provider.define(field, query);
    }

    @Override
    public void onDiscover(Parameter arg, Inject metadata) {
        if (isDefined(metadata.value())) {
            UnitQuery query = new UnitQuery(metadata.value(), arg.getType(), null);
            provider.define(arg, query);
        }
    }

}
