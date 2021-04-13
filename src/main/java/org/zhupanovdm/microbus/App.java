package org.zhupanovdm.microbus;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.ActivationLauncher;
import org.zhupanovdm.microbus.core.AppContext;
import org.zhupanovdm.microbus.core.AppDefaultContext;
import org.zhupanovdm.microbus.core.di.CreationStrategy;
import org.zhupanovdm.microbus.core.di.InstanceProvider;
import org.zhupanovdm.microbus.core.reflector.AnnotationRegistry;
import org.zhupanovdm.microbus.core.reflector.PackageScanner;

import java.util.Arrays;

@Slf4j
public class App {
    private static AppContext context;

    public static void run(Class<?> mainClass, String[] args) {
        log.info("Launching app with args: {}", Arrays.toString(args));
        context = AppDefaultContext.create(mainClass, args);

        InstanceProvider instanceProvider = context.getInstanceProvider();
        instanceProvider.registerCreationStrategy(new CreationStrategy.Singleton());
        instanceProvider.registerCreationStrategy(new CreationStrategy.Factory());

        PackageScanner packageScanner = new PackageScanner();

        AnnotationRegistry annotationRegistry = context.getAnnotationRegistry();
        annotationRegistry.scan(packageScanner.scan(App.class.getPackageName()));
        annotationRegistry.scan(packageScanner.scan(mainClass.getPackageName()));

        new ActivationLauncher(annotationRegistry, context.getActivatorRegistry())
                .scan()
                .engage();
    }

    public static void shutdown() {
        context.destroy();
    }

    public static AppContext getContext() {
        return context;
    }

}
