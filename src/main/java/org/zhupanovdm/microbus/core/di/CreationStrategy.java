package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.zhupanovdm.microbus.util.CommonUtils.doWithLock;

@Slf4j
public abstract class CreationStrategy {
    public abstract Object getInstance(UnitHolder unit, Supplier<?> spawner);

    @ThreadSafe
    public static class Singleton extends CreationStrategy {
        private final Lock lock = new ReentrantLock();

        @Override
        public Object getInstance(UnitHolder unit, Supplier<?> spawner) {
            if (unit.getInstance() != null)
                return unit.getInstance();
            return doWithLock(lock, () -> {
                if (unit.getInstance() == null)
                    unit.setInstance(spawner.get());
                return unit.getInstance();
            });
        }
    }

    @ThreadSafe
    public static class Factory extends CreationStrategy {
        @Override
        public Object getInstance(UnitHolder unit, Supplier<?> spawner) {
            return spawner.get();
        }
    }

}
