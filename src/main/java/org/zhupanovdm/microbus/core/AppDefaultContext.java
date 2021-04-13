package org.zhupanovdm.microbus.core;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.di.*;
import org.zhupanovdm.microbus.core.reflector.AnnotationRegistry;
import org.zhupanovdm.microbus.core.reflector.PackageScanner;

@Slf4j
public class AppDefaultContext implements AppContext {
    private final Class<?> appClass;
    private final String[] args;
    private final AnnotationRegistry annotationRegistry;
    private final ActivatorRegistry activatorRegistry;
    private final UnitRegistry unitRegistry;
    private final InstanceProvider instanceProvider;
    private final DependencyQualifierProvider qualifierProvider;

    private AppDefaultContext(Class<?> appClass, String[] args) {
        this.appClass = appClass;
        this.args = args;

        this.annotationRegistry = new AnnotationRegistry();
        this.activatorRegistry = new ActivatorRegistry();
        this.unitRegistry = new UnitRegistry();

        this.qualifierProvider = new DependencyQualifierProvider(
                new DefaultDependencyQualifier<>(UnitQuery::of),
                new DefaultDependencyQualifier<>(UnitQuery::of),
                new DefaultDependencyQualifier<>());
        this.instanceProvider = new InstanceProvider(this.unitRegistry, this.qualifierProvider);
    }

    public static AppContext create(Class<?> appClass, String[] args) {
        return new AppDefaultContext(appClass, args);
    }

    @Override
    public String getName() {
        return appClass.getCanonicalName();
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public AnnotationRegistry getAnnotationRegistry() {
        return annotationRegistry;
    }

    @Override
    public ActivatorRegistry getActivatorRegistry() {
        return activatorRegistry;
    }

    @Override
    public UnitRegistry getUnitRegistry() {
        return unitRegistry;
    }

    @Override
    public InstanceProvider getInstanceProvider() {
        return instanceProvider;
    }

    @Override
    public DependencyQualifierProvider getQualifierProvider() {
        return qualifierProvider;
    }

    @Override
    public void destroy() {

    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), getName());
    }

}
