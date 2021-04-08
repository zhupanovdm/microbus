package org.zhupanovdm.microbus.core.unit;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.zhupanovdm.microbus.utils.CommonUtils.doWithLock;

@Slf4j
public abstract class CreationStrategy {
    public abstract Object getInstance(Supplier<?> spawner, Consumer<Object> initializer);

    @ThreadSafe
    public static class Singleton extends CreationStrategy {
        private Object instance;
        private final Lock lock = new ReentrantLock();

        @Override
        public Object getInstance(Supplier<?> spawner, Consumer<Object> initializer) {
            if (instance == null) {
                doWithLock(lock, () -> {
                    if (instance == null)
                        initializer.accept(instance = spawner.get());
                    return null;
                });
            }
            return instance;
        }
    }

    @ThreadSafe
    public static class Factory extends CreationStrategy {
        @Override
        public Object getInstance(Supplier<?> spawner, Consumer<Object> initializer) {
            Object instance = spawner.get();
            initializer.accept(instance);
            return instance;
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
