package org.zhupanovdm.microbus.core.di;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.zhupanovdm.microbus.CommonUtils.doWithLock;


public abstract class CreationStrategy {
    public abstract <T> T getInstance(InstanceHolder<T> holder, Supplier<T> spawner);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @ThreadSafe
    public static class Singleton extends CreationStrategy {
        private final Lock lock = new ReentrantLock();

        @Override
        public <T> T getInstance(InstanceHolder<T> holder, Supplier<T> spawner) {
            if (holder.getInstance() != null)
                return holder.getInstance();
            return doWithLock(lock, () -> {
                if (holder.getInstance() == null)
                    holder.setInstance(spawner.get());
                return holder.getInstance();
            });
        }
    }

    @ThreadSafe
    public static class Factory extends CreationStrategy {
        @Override
        public <T> T getInstance(InstanceHolder<T> holder, Supplier<T> spawner) {
            return spawner.get();
        }
    }

}
