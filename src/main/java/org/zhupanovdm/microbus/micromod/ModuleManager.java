package org.zhupanovdm.microbus.micromod;

import org.zhupanovdm.microbus.micromod.spawner.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ModuleManager {
    private final Map<Class<? extends InstanceProvider>, Function<Spawner, InstanceProvider>> spawnStrategyFactory;
    private final ModuleRegistry registry;

    public ModuleManager(ModuleRegistry registry) {
        this.registry = registry;
        this.spawnStrategyFactory = new HashMap<>();
        this.spawnStrategyFactory.put(InstanceProvider.Singleton.class, InstanceProvider.Singleton::new);
        this.spawnStrategyFactory.put(InstanceProvider.Factory.class, InstanceProvider.Factory::new);
    }

    public void register(String id, Class<?> type, Class<? extends InstanceProvider> providerType) {
        InstanceProvider provider = spawnStrategyFactory.get(providerType).apply(new ClassSpawner(type, new Initializer(type)));
        Module module = new Module(id, type, provider);
        registry.register(module);
    }

    public void register(String id, Method method, String declaringModuleId, Class<? extends InstanceProvider> providerType) {
        Class<?> type = method.getReturnType();
        InstanceProvider provider = spawnStrategyFactory.get(providerType).apply(new MethodSpawner(method, declaringModuleId, new Initializer(type)));
        Module module = new Module(id, type, provider);
        registry.register(module);
    }

    public void unregister(String id) {
        Module module = registry.request(ModuleQuery.of(id));
        if (module != null) {
            registry.unregister(module);
        }
    }

    public Object resolve(ModuleQuery query) {
        Module module = registry.request(query);
        if (module == null)
            throw new IllegalStateException("Cant satisfy request: " + query);
        return resolve(module, new LinkedHashSet<>());
    }

    private Object resolve(Module module, Set<Module> chain) {
        if (!chain.add(module))
            throw new IllegalStateException("Recursive module dependency: " + module + " > " + chain);
        Object instance = module.getInstance(dependencies ->
                dependencies.forEach(dependency -> dependency.getInjector()
                .accept(resolve(registry.request(dependency), chain))));
        chain.remove(module);
        return instance;
    }

}