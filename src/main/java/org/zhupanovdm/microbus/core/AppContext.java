package org.zhupanovdm.microbus.core;

import org.zhupanovdm.microbus.core.activator.ActivatorRegistry;
import org.zhupanovdm.microbus.core.di.DependencyQualifierProvider;
import org.zhupanovdm.microbus.core.di.InstanceCreationStrategyProvider;
import org.zhupanovdm.microbus.core.di.InstanceProvider;
import org.zhupanovdm.microbus.core.di.UnitRegistry;
import org.zhupanovdm.microbus.core.reflector.AnnotationRegistry;

public interface AppContext {
    String getName();
    String[] getArgs();

    AnnotationRegistry getAnnotationRegistry();
    ActivatorRegistry getActivatorRegistry();
    UnitRegistry getUnitRegistry();

    InstanceProvider getInstanceProvider();
    DependencyQualifierProvider getQualifierProvider();
    InstanceCreationStrategyProvider getInstanceCreationStrategyProvider();

    void destroy();
}
