package org.zhupanovdm.microbus.micromod.spawner;

import org.zhupanovdm.microbus.micromod.Module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface SpawnStrategy extends BiFunction<Module, Function<Module, Object>, Object> {
    class Singleton implements SpawnStrategy {
        private final Map<Module, Object> instances = new HashMap<>();
        @Override
        public Object apply(Module module, Function<Module, Object> spawn) {
            if (!instances.containsKey(module))
                instances.put(module, spawn.apply(module));
            return instances.get(module);
        }
    }

    class Factory implements SpawnStrategy {
        @Override
        public Object apply(Module module, Function<Module, Object> spawn) {
            return spawn.apply(module);
        }
    }
}
