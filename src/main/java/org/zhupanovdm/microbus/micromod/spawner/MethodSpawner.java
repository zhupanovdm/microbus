package org.zhupanovdm.microbus.micromod.spawner;

import org.zhupanovdm.microbus.micromod.ModuleQuery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class MethodSpawner extends Spawner {
    private final Method method;
    private final String declaringModuleId;
    private Object declaringModuleObject;
    private final Object[] args;

    public MethodSpawner(Method method, String declaringModuleId, Initializer initializer) {
        super(initializer);

        this.method = method;
        this.declaringModuleId = declaringModuleId;
        this.args = new Object[method.getParameters().length];
    }

    @Override
    public Collection<ModuleQuery> getDependencies() {
        List<ModuleQuery> dependencies = new LinkedList<>();
        dependencies.add(ModuleQuery.of(declaringModuleId, method.getDeclaringClass(), true).withInjector(o -> declaringModuleObject = o));
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++)
            dependencies.add(ModuleQuery.inject(parameters[i], createArgInjector(i)));
        return Collections.unmodifiableList(dependencies);
    }

    private Consumer<Object> createArgInjector(int i) {
        return o -> args[i] = o;
    }

    @Override
    protected Object instantiate() {
        try {
            return method.invoke(declaringModuleObject, args);
        } catch (IllegalAccessException e) {
            method.setAccessible(true);
            Object instance = instantiate();
            method.setAccessible(false);
            return instance;
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error on invocation module spawning method: " + method, e);
        }
    }
}
