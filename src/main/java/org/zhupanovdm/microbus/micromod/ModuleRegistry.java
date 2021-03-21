package org.zhupanovdm.microbus.micromod;

import lombok.NonNull;
import org.zhupanovdm.microbus.utils.ClassMappedValueScanner;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class ModuleRegistry {
    private final Set<Module> registered = new HashSet<>();
    private final Map<String, Module> names = new HashMap<>();
    private final ClassMappedValueScanner<Module> types = new ClassMappedValueScanner<>(Module::getType);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void register(@NonNull Module module) {
        doWithLock(lock.writeLock(), () -> {
            if (!registered.add(module))
                throw new IllegalArgumentException("Module is already registered " + module);
            names.put(module.getId(), module);
            types.put(module);
            return null;
        });
    }

    public void unregister(@NonNull Module module) {
        doWithLock(lock.writeLock(), () -> {
            if (registered.remove(module)) {
                names.remove(module.getId());
                types.remove(module);
            }
            return null;
        });
    }

    public Module request(@NonNull ModuleQuery query) {
        return doWithLock(lock.readLock(), () -> {
            if (query.hasId()) {
                Module module = names.get(query.getId());
                if (module != null && query.isTypeCompatibleWith(module)) {
                    return module;
                } else if (query.isStrictId()) {
                    return null;
                }
            }
            if (query.getType() == null)
                return null;
            Set<Module> discovered = types.collect(query.getType(), new HashSet<>());
            if (discovered.size() > 1)
                throw new IllegalStateException("Ambiguous request " + query + " has discovered several registered modules " + discovered);
            return discovered.stream().findFirst().orElse(null);
        });
    }

    public Set<Module> getModules() {
        return Collections.unmodifiableSet(registered);
    }

    private <T> T doWithLock(Lock lock, Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

}
