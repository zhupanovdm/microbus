package org.zhupanovdm.microbus.core;

import org.zhupanovdm.microbus.core.di.DependencyQualifierProvider;
import org.zhupanovdm.microbus.core.di.InstanceProvider;
import org.zhupanovdm.microbus.core.di.UnitRegistry;
import org.zhupanovdm.microbus.core.reflector.AnnotationRegistry;

public interface AppContext {
    String getName();
    String[] getArgs();
    AnnotationRegistry getAnnotationRegistry();
    UnitRegistry getUnitRegistry();
    InstanceProvider getInstanceProvider();
    DependencyQualifierProvider getQualifierProvider();
}
