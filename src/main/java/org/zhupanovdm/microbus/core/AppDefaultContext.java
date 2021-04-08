package org.zhupanovdm.microbus.core;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.reflector.AnnotationsRegistry;
import org.zhupanovdm.microbus.core.reflector.PackageScanner;
import org.zhupanovdm.microbus.core.di.DependencyQualifier;
import org.zhupanovdm.microbus.core.di.ObjectInitializer;
import org.zhupanovdm.microbus.core.di.UnitQuery;
import org.zhupanovdm.microbus.core.di.UnitRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@Slf4j
public class AppDefaultContext implements AppContext {
    private final Class<?> appClass;
    private final String[] args;
    private AnnotationsRegistry annotationsRegistry;
    private DependencyQualifier<Field> fieldQualifier;
    private DependencyQualifier<Parameter> argumentQualifier;
    private ObjectInitializer objectInitializer;
    private UnitRegistry unitRegistry;

    private AppDefaultContext(Class<?> appClass, String[] args) {
        this.appClass = appClass;
        this.args = args;
    }

    private void init() {
        PackageScanner packageScanner = new PackageScanner();

        annotationsRegistry = new AnnotationsRegistry();
        annotationsRegistry.scan(packageScanner.scan(App.class.getPackageName()));
        annotationsRegistry.scan(packageScanner.scan(appClass.getPackageName()));

        argumentQualifier = new DependencyQualifier<>(UnitQuery::of);
        fieldQualifier = new DependencyQualifier<>(UnitQuery::of);
        unitRegistry = new UnitRegistry();
        objectInitializer = new ObjectInitializer(unitRegistry, fieldQualifier);
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
    public AnnotationsRegistry getAnnotationsRegistry() {
        return annotationsRegistry;
    }

    @Override
    public UnitRegistry getUnitRegistry() {
        return unitRegistry;
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
