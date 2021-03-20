package org.zhupanovdm.microbus.core;

import com.google.common.collect.Table;
import org.zhupanovdm.microbus.micromod.ModuleManager;
import org.zhupanovdm.microbus.micromod.ModuleQuery;
import org.zhupanovdm.microbus.micromod.ModuleRegistry;
import org.zhupanovdm.microbus.micromod.annotations.MicroModule;
import org.zhupanovdm.microbus.micromod.reflector.AnnotationsRegistry;
import org.zhupanovdm.microbus.micromod.reflector.PackageScanner;
import org.zhupanovdm.microbus.utils.CommonUtils;
import org.zhupanovdm.microbus.utils.ReflectorUtils;

import java.util.Set;

public class ApplicationRunner {
    public static void run(Class<?> mainClass, String[] args) {
        ModuleRegistry moduleRegistry = new ModuleRegistry();
        ModuleManager moduleManager = new ModuleManager(moduleRegistry);

        AnnotationsRegistry annotationsRegistry = new AnnotationsRegistry();
        annotationsRegistry.scan(new PackageScanner().scan(mainClass.getPackageName()));

        Table<Class<?>, MicroModule, Integer> result = annotationsRegistry.getClasses().scan(MicroModule.class);
        Set<Class<?>> classes = result.rowKeySet();
        for (Class<?> clazz : classes) {
            Set<MicroModule> microModules = ReflectorUtils.withHighestPriority(clazz, result);
            if (microModules.size() > 1) {
                throw new IllegalStateException("Ambiguous annotations");
            }
            MicroModule microModule = CommonUtils.anyOf(microModules);
            if (microModule == null)
                continue;
            moduleManager.registerModule(clazz.getSimpleName(), clazz, microModule.spawn());
        }

        System.out.println(moduleRegistry.getRegisteredModules());

        Object resolve = moduleManager.resolve(ModuleQuery.of(Class100.class));
        System.out.println("resolve: " + resolve);
    }
}
