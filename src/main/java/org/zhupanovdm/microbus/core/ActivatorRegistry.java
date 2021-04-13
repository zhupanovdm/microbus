package org.zhupanovdm.microbus.core;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.activator.ActivatorTemplate;
import org.zhupanovdm.microbus.core.annotation.Activator;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static org.zhupanovdm.microbus.util.CommonUtils.doWithLock;

@Slf4j
@ThreadSafe
public class ActivatorRegistry {
    private final Map<Class<?>, Activator> metadata = new HashMap<>();
    private final Set<Class<?>> activators = new HashSet<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void register(Class<?> type, Activator annotation) {
        doWithLock(lock.writeLock(), () -> {
            metadata.put(type, annotation);
            activators.add(type);
            return type;
        });
        log.debug("Registered activator {}: {}", activators, annotation);
    }

    public Activator getMetadata(Class<?> type) {
        return doWithLock(lock.readLock(), () -> metadata.get(type));
    }

    public List<ActivatorTemplate<?>> collect() {
        return doWithLock(lock.readLock(), () -> activators.stream()
                .sorted(Comparator.comparingInt(type -> metadata.get(type).priority()))
                .map(this::createActivator)
                .collect(Collectors.toUnmodifiableList()));
    }

    private ActivatorTemplate<?> createActivator(Class<?> type) {
        try {
            return (ActivatorTemplate<?>) type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Failed to instantiate activator {}", type, e);
            throw new RuntimeException("Failed to instantiate activator " + type, e);
        }
    }

}
