package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.zhupanovdm.microbus.util.CommonUtils.doWithLock;

@Slf4j
public abstract class CreationStrategy {
    public abstract Object getInstance(Supplier<?> spawner);

    @ThreadSafe
    public static class Singleton extends CreationStrategy {
        private Object instance;
        private final Lock lock = new ReentrantLock();

        @Override
        public Object getInstance(Supplier<?> spawner) {
            if (instance == null) {
                doWithLock(lock, () -> {
                    if (instance == null) instance = spawner.get();
                    return instance;
                });
            }
            return instance;
        }
    }

    @ThreadSafe
    public static class Factory extends CreationStrategy {
        @Override
        public Object getInstance(Supplier<?> spawner) {
            return spawner.get();
        }
    }

    public static CreationStrategy create(Class<? extends CreationStrategy> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error("Unable to create mod instance provider {}", type, e);
            throw new RuntimeException("Constructor invocation failed", e);
        }
    }

}
