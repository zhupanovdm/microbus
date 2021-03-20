package org.zhupanovdm.microbus.micromod;

import org.zhupanovdm.microbus.micromod.spawner.ClassSpawner;
import org.zhupanovdm.microbus.micromod.spawner.Initializer;
import org.zhupanovdm.microbus.micromod.spawner.SpawnStrategy;
import org.zhupanovdm.microbus.micromod.spawner.MethodSpawner;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ModuleManager {
    private final Map<Class<? extends SpawnStrategy>, SpawnStrategy> spawnStrategies;
    private final ModuleRegistry registry;

    public ModuleManager(ModuleRegistry registry) {
        this.registry = registry;
        this.spawnStrategies = new HashMap<>();
        addSpawnStrategy(new SpawnStrategy.Singleton());
        addSpawnStrategy(new SpawnStrategy.Factory());
    }

    public void registerModule(String id, Class<?> type, Class<? extends SpawnStrategy> provider) {
        Module module = new Module(id, type, provider);
        module.setSpawner(new ClassSpawner(type));
        module.setInitializer(new Initializer(type));
        registry.register(module);
    }

    public void registerModule(String id, Method method, String declaringModuleId, Class<? extends SpawnStrategy> provider) {
        Module module = new Module(id, method.getReturnType(), provider);
        module.setSpawner(new MethodSpawner(method, declaringModuleId));
        module.setInitializer(new Initializer(method.getReturnType()));
        registry.register(module);
    }

    public void unregisterModule(String id) {
        Module module = registry.request(ModuleQuery.of(id));
        if (module != null) {
            registry.unregister(module);
        }
    }

    public synchronized Object resolve(ModuleQuery query) {
        Module module = registry.request(query);
        if (module == null)
            throw new IllegalStateException("Cant satisfy request: " + query);
        return resolve(module, new LinkedHashSet<>());
    }

    private Object resolve(Module module, Set<Module> chain) {
        if (module == null)
            throw new IllegalArgumentException("Cant resolve chain: " + chain);

        return instantiate(module, m -> {
            if (!chain.add(m))
                throw new IllegalStateException("Recursive module dependency");

            for (ModuleQuery query : m.getSpawner().getDependencies()) {
                Object obj = resolve(registry.request(query), chain);
                query.getInjector().accept(obj);

                Initializer initializer = m.getInitializer();
                for (ModuleQuery query1 : initializer.getDependencies())
                    query.getInjector().accept(resolve(registry.request(query1), chain));
                initializer.init(obj);
            }
            chain.remove(m);
            return m.getSpawner().get();
        });
    }

    private void addSpawnStrategy(SpawnStrategy strategy) {
        spawnStrategies.put(strategy.getClass(), strategy);
    }

    private Object instantiate(Module module, Function<Module, Object> spawner) {
        return spawnStrategies.get(module.getSpawnStrategy()).apply(module, spawner);
    }

}