package org.zhupanovdm.microbus.micromod.spawner;

import org.zhupanovdm.microbus.micromod.Module;
import org.zhupanovdm.microbus.micromod.ModuleManager;
import org.zhupanovdm.microbus.micromod.ModuleQuery;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class ConstructorSpawner extends Spawner {
    private final Constructor<?> constructor;

    public ConstructorSpawner(Module module, ModuleManager manager) {
        super(module, manager);
        Constructor<?>[] constructors = module.getType().getDeclaredConstructors();
        if (constructors.length != 1)
            throw new IllegalArgumentException("Only one constructor is supported for module: " + module);
        this.constructor = constructors[0];
    }

    @Override
    protected Object doSpawn(Function<ModuleQuery, Object> injector) {
        return instantiate(argsOf(constructor, injector));
    }

    private Object instantiate(Object[] args) {
        try {
            return constructor.newInstance(args);
        } catch (IllegalAccessException e) {
            constructor.setAccessible(true);
            Object instance = instantiate(args);
            constructor.setAccessible(false);
            return instance;
        } catch (InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Error on invocation module constructor: " + module, e);
        }
    }
}
