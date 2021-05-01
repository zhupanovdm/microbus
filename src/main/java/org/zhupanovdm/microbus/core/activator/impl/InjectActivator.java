package org.zhupanovdm.microbus.core.activator.impl;

import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.activator.ActivatorTemplate;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.annotation.Inject;
import org.zhupanovdm.microbus.core.di.DependencyQualifierProvider;
import org.zhupanovdm.microbus.core.di.UnitQuery;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@Activator(marker = Inject.class, priority = 11)
@SuppressWarnings("unused")
public class InjectActivator implements ActivatorTemplate<Inject> {
    private final DependencyQualifierProvider provider;

    public InjectActivator() {
        provider = App.getContext().getQualifierProvider();
    }

    @Override
    public void onDiscover(Field field, Inject metadata) {
        provider.define(field, UnitQuery.create().from(field).from(metadata).build());
    }

    @Override
    public void onDiscover(Parameter arg, Inject metadata) {
        provider.define(arg, UnitQuery.create().from(arg).from(metadata).build());
    }

}
