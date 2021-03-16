package org.zhupanovdm.microbus.micromod.spawner;

import org.zhupanovdm.microbus.micromod.Module;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface InstanceProvider extends BiFunction<Module, Function<Module, Object>, Object> {
    class Singleton implements InstanceProvider {
        private final Map<Module, Object> instances = new ConcurrentHashMap<>();
        @Override
        public Object apply(Module module, Function<Module, Object> spawner) {
            return instances.computeIfAbsent(module, spawner);
        }
    }
    class Factory implements InstanceProvider {
        @Override
        public Object apply(Module module, Function<Module, Object> spawner) {
            return spawner.apply(module);
        }
    }
}
