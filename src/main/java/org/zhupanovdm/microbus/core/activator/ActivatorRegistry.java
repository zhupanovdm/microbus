package org.zhupanovdm.microbus.core.activator;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.AppContext;
import org.zhupanovdm.microbus.core.di.CreationStrategy;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@ThreadSafe
public class ActivatorRegistry {
    private final AppContext context;
    private final Map<Class<?>, ActivatorHolder> activators;

    public ActivatorRegistry(AppContext context) {
        this.context = context;
        this.activators = new ConcurrentHashMap<>();
    }

    public void register(ActivatorHolder holder) {
        activators.put(holder.getType(), holder);
        log.debug("Registered activator {}", holder);
    }

    public List<ActivatorTemplate<?>> getAll() {
        return activators.values().stream()
                .sorted(Comparator.comparingInt(ActivatorHolder::getPriority))
                .map(this::getInstance)
                .collect(Collectors.toUnmodifiableList());
    }

    public ActivatorHolder get(Class<?> type) {
        return activators.get(type);
    }

    private ActivatorTemplate<?> getInstance(ActivatorHolder holder) {
        CreationStrategy strategy = context.getInstanceCreationStrategyProvider().get(holder.getStrategy());
        return strategy.getInstance(holder, () -> {
            try {
                return (ActivatorTemplate<?>) holder.getType().getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error("Failed to instantiate activator {}", holder.getType(), e);
                throw new RuntimeException("Failed to instantiate activator " + holder.getType(), e);
            }
        });
    }

}
