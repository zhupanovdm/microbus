package org.zhupanovdm.microbus.micromod.spawner;

import org.zhupanovdm.microbus.micromod.Module;
import org.zhupanovdm.microbus.micromod.ModuleManager;
import org.zhupanovdm.microbus.micromod.ModuleQuery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class MethodSpawner extends Spawner {
    private final Method method;
    private final String declaringModuleId;

    public MethodSpawner(Module module, Method method, String declaringModuleId, ModuleManager manager) {
        super(module, manager);
        this.method = method;
        this.declaringModuleId = declaringModuleId;
    }

    @Override
    public Object doSpawn(Function<ModuleQuery, Object> injector) {
        return instantiate(injector.apply(ModuleQuery.create(declaringModuleId, method.getDeclaringClass(), true)), argsOf(method, injector));
    }

    private Object instantiate(Object object, Object[] args) {
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            method.setAccessible(true);
            Object instance = instantiate(object, args);
            method.setAccessible(false);
            return instance;
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error on invocation module spawning method: " + method + " module: " + module, e);
        }
    }

}
