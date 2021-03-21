package org.zhupanovdm.microbus.micromod.spawner;

import org.zhupanovdm.microbus.micromod.ModuleQuery;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ClassSpawner extends Spawner {
    private final Constructor<?> constructor;
    private final Object[] args;

    public ClassSpawner(Class<?> clazz, Initializer initializer) {
        super(initializer);

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length != 1)
            throw new IllegalArgumentException("Only one constructor is supported for class spawning: " + clazz);
        this.constructor = constructors[0];
        this.args = new Object[constructors[0].getParameters().length];
    }

    @Override
    public Collection<ModuleQuery> getDependencies() {
        List<ModuleQuery> dependencies = new LinkedList<>();
        Parameter[] parameters = constructor.getParameters();
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
            return constructor.newInstance(args);
        } catch (IllegalAccessException e) {
            constructor.setAccessible(true);
            Object instance = instantiate();
            constructor.setAccessible(false);
            return instance;
        } catch (InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Error on invocation spawning constructor: " + constructor, e);
        }
    }
}
