package org.zhupanovdm.microbus;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.activator.ActivatorHolder;
import org.zhupanovdm.microbus.core.activator.ActivatorLauncher;
import org.zhupanovdm.microbus.core.activator.ActivatorRegistry;
import org.zhupanovdm.microbus.core.AppContext;
import org.zhupanovdm.microbus.core.AppDefaultContext;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.di.UnitQuery;
import org.zhupanovdm.microbus.core.di.UnitQuery.Option;
import org.zhupanovdm.microbus.core.reflector.AnnotatedElementHolder;
import org.zhupanovdm.microbus.core.reflector.AnnotationRegistry;
import org.zhupanovdm.microbus.core.reflector.PackageScanner;

import java.util.Arrays;

@Slf4j
public class App {
    private static AppContext context;

    public static void run(Class<?> mainClass, String[] args) {
        log.info("Launching app with args: {}", Arrays.toString(args));
        context = AppDefaultContext.create(mainClass, args);

        AnnotationRegistry annotationRegistry = context.getAnnotationRegistry();
        annotationRegistry.scan(PackageScanner.scan(ActivatorLauncher.class.getPackageName()));
        annotationRegistry.scan(PackageScanner.scan(mainClass.getPackageName()));

        ActivatorRegistry registry = context.getActivatorRegistry();
        CommonUtils.forEachRow(annotationRegistry.getClasses().scan(Activator.class),
                (type, table) -> registry.register(new ActivatorHolder(type, AnnotatedElementHolder.getSingle(type, table))));

        activate(null);
    }

    public static void shutdown() {
        context.destroy();
    }

    public static AppContext getContext() {
        return context;
    }

    public static void scan(String packageName) {
        context.getAnnotationRegistry().scan(PackageScanner.scan(packageName));
    }

    public static void activate(String packageNames) {
        new ActivatorLauncher(context, packageNames).engage();
    }

    public static <T> T getUnit(String id, Class<T> type, Option ...options) {
        //noinspection unchecked
        return (T) context.getInstanceProvider().resolve(new UnitQuery(id, type, null, options));
    }

    public static Object getUnit(String id) {
        return getUnit(id, null);
    }

    public static <T> T getUnit(Class<T> type) {
        return getUnit(null, type);
    }

}
