package org.zhupanovdm.microbus.micromod.spawner;

import lombok.NonNull;
import org.zhupanovdm.microbus.micromod.Module;
import org.zhupanovdm.microbus.micromod.ModuleManager;
import org.zhupanovdm.microbus.micromod.ModuleQuery;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.function.Function;

public abstract class Spawner {
    protected final Module module;
    protected final ModuleManager manager;

    public Spawner(@NonNull Module module, @NonNull ModuleManager manager) {
        this.module = module;
        this.manager = manager;
    }

    public Object spawn(Set<Module> chain) {
        Function<ModuleQuery, Object> injector = getInjector(chain);
        InstanceProvider provider = manager.getInstanceProvider(module.getInstanceProviderType());
        Object instance = provider.apply(module, m -> init(doSpawn(injector), injector));
        chain.remove(module);
        return instance;
    }

    protected abstract Object doSpawn(Function<ModuleQuery, Object> injector);

    protected Object init(Object instance, Function<ModuleQuery, Object> injector) {
        if (module.getInitFields() == null)
            return instance;

        for (var field : module.getInitFields())
            initField(field, instance, injector.apply(ModuleQuery.create(field)));

        return instance;
    }

    private void initField(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            initField(field, instance, value);
            field.setAccessible(false);
        }
    }

    private Function<ModuleQuery, Object> getInjector(Set<Module> chain) {
        if (!chain.add(module))
            throw new IllegalStateException("Cyclic module dependency chain detected: " + chain + " while satisfying " + module);
        return query -> manager.resolve(query.withChain(chain));
    }

    protected static Object[] argsOf(Executable executable, Function<ModuleQuery, Object> injector) {
        Parameter[] parameters = executable.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            args[i] = injector.apply(ModuleQuery.create(parameters[i]));
        }
        return args;
    }

}
