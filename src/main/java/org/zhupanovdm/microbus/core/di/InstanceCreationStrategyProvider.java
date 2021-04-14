package org.zhupanovdm.microbus.core.di;

import lombok.extern.log4j.Log4j2;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@ThreadSafe
public class InstanceCreationStrategyProvider {
    private final Map<Class<? extends CreationStrategy>, CreationStrategy> strategies = new ConcurrentHashMap<>();

    public CreationStrategy register(CreationStrategy strategy) {
        return strategies.put(strategy.getClass(), strategy);
    }

    public CreationStrategy unregister(Class<? extends CreationStrategy> type) {
        return strategies.remove(type);
    }

    public CreationStrategy get(Class<? extends CreationStrategy> type) {
        return strategies.computeIfAbsent(type, aClass -> {
            try {
                return aClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error("Failed to instantiate strategy type: {}", type, e);
                throw new RuntimeException("Failed to instantiate " + type, e);
            }
        });
    }

}
