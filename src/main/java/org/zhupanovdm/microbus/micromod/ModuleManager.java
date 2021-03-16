package org.zhupanovdm.microbus.micromod;

import org.zhupanovdm.microbus.micromod.spawner.ConstructorSpawner;
import org.zhupanovdm.microbus.micromod.spawner.InstanceProvider;
import org.zhupanovdm.microbus.micromod.spawner.MethodSpawner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleManager {
    private final Map<Class<? extends InstanceProvider>, InstanceProvider> instanceProviders;
    private final ModuleRegistry registry;

    public ModuleManager(ModuleRegistry registry) {
        this.instanceProviders = new ConcurrentHashMap<>();
        this.registry = registry;
    }

    public void registerInstanceProvider(InstanceProvider provider) {
        instanceProviders.put(provider.getClass(), provider);
    }

    public InstanceProvider getInstanceProvider(Class<? extends InstanceProvider> type) {
        return instanceProviders.get(type);
    }

    public void registerModule(String id, Class<?> type, Class<? extends InstanceProvider> provider, List<Field> initFields) {
        Module module = new Module(id, type, provider);
        module.setSpawner(new ConstructorSpawner(module, this));
        module.setInitFields(initFields);
        registry.register(module);
    }

    public void registerModule(String id, Method method, String declaringModuleId, Class<? extends InstanceProvider> provider, List<Field> initFields) {
        Module module = new Module(id, method.getReturnType(), provider);
        module.setSpawner(new MethodSpawner(module, method, declaringModuleId, this));
        module.setInitFields(initFields);
        registry.register(module);
    }

    public void unregisterModule(String id) {
        Module module = registry.request(ModuleQuery.create(id));
        if (module != null) {
            registry.unregister(module);
        }
    }

    public Object resolve(ModuleQuery query) {
        Module module = registry.request(query);
        if (module == null) {
            throw new IllegalStateException("Cannot satisfy dependency " + query);
        }
        if (query.getChain() == null)
            query.withChain(new LinkedHashSet<>());
        return module.getSpawner().spawn(query.getChain());
    }

}