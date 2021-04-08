package org.zhupanovdm.microbus.core;

import org.zhupanovdm.microbus.core.reflector.AnnotationsRegistry;
import org.zhupanovdm.microbus.core.di.DependencyQualifier;
import org.zhupanovdm.microbus.core.di.ObjectInitializer;
import org.zhupanovdm.microbus.core.di.UnitRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public interface AppContext {
    String getName();
    String[] getArgs();
    AnnotationsRegistry getAnnotationsRegistry();
    UnitRegistry getUnitRegistry();
    DependencyQualifier<Field> getFieldQualifier();
    DependencyQualifier<Parameter> getArgumentQualifier();
    ObjectInitializer getObjectInitializer();
}
