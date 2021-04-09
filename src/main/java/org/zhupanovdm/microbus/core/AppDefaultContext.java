package org.zhupanovdm.microbus.core;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.di.DependencyQualifier;
import org.zhupanovdm.microbus.core.di.ObjectInitializer;
import org.zhupanovdm.microbus.core.di.UnitQuery;
import org.zhupanovdm.microbus.core.di.UnitRegistry;
import org.zhupanovdm.microbus.core.reflector.AnnotationRegistry;
import org.zhupanovdm.microbus.core.reflector.PackageScanner;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@Slf4j
public class AppDefaultContext implements AppContext {
    private final Class<?> appClass;
    private final String[] args;
    private final AnnotationRegistry annotationRegistry;
    private final DependencyQualifier<Executable> executableTargetQualifier;
    private final DependencyQualifier<Field> fieldQualifier;
    private final DependencyQualifier<Parameter> argumentQualifier;
    private final ObjectInitializer objectInitializer;
    private final UnitRegistry unitRegistry;

    private AppDefaultContext(Class<?> appClass, String[] args) {
        this.appClass = appClass;
        this.args = args;

        annotationRegistry = new AnnotationRegistry();
        argumentQualifier = new DependencyQualifier<>(UnitQuery::of);
        fieldQualifier = new DependencyQualifier<>(UnitQuery::of);
        executableTargetQualifier = new DependencyQualifier<>();
        unitRegistry = new UnitRegistry();
        objectInitializer = new ObjectInitializer(this);
    }

    private void init() {
        PackageScanner packageScanner = new PackageScanner();
        annotationRegistry.scan(packageScanner.scan(App.class.getPackageName()));
        annotationRegistry.scan(packageScanner.scan(appClass.getPackageName()));
    }

    public static AppContext create(Class<?> appClass, String[] args) {
        AppDefaultContext context = new AppDefaultContext(appClass, args);
        context.init();
        return context;
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
    public UnitRegistry getUnitRegistry() {
        return unitRegistry;
    }

    @Override
    public DependencyQualifier<Executable> getExecutableTargetQualifier() {
        return executableTargetQualifier;
    }

    @Override
    public DependencyQualifier<Field> getFieldQualifier() {
        return fieldQualifier;
    }

    @Override
    public ObjectInitializer getObjectInitializer() {
        return objectInitializer;
    }

    @Override
    public DependencyQualifier<Parameter> getArgumentQualifier() {
        return argumentQualifier;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), getName());
    }
}
