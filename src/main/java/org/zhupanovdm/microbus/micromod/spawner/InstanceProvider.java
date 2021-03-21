package org.zhupanovdm.microbus.micromod.spawner;

import lombok.Data;
import org.zhupanovdm.microbus.micromod.ModuleQuery;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

@Data
public abstract class InstanceProvider implements Function<Consumer<Collection<ModuleQuery>>, Object> {
    protected final Spawner spawner;

    public InstanceProvider(Spawner spawner) {
        this.spawner = spawner;
    }

    public static class Singleton extends InstanceProvider {
        private Object instance;
        private final Lock locker = new ReentrantLock();

        public Singleton(Spawner spawner) {
            super(spawner);
        }

        @Override
        public Object apply(Consumer<Collection<ModuleQuery>> injector) {
            if (instance == null) {
                locker.lock();
                try {
                    if (instance == null)
                        instance = spawner.apply(injector);
                } finally {
                    locker.unlock();
                }
            }
            return instance;
        }
    }

    public static class Factory extends InstanceProvider {
        public Factory(Spawner spawner) {
            super(spawner);
        }

        @Override
        public Object apply(Consumer<Collection<ModuleQuery>> injector) {
            return spawner.apply(injector);
        }
    }
}
