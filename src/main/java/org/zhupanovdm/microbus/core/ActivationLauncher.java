package org.zhupanovdm.microbus.core;

import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.activator.ActivatorTemplate;
import org.zhupanovdm.microbus.core.annotations.Activator;
import org.zhupanovdm.microbus.core.reflector.AnnotatedElementsHolder;
import org.zhupanovdm.microbus.core.reflector.AnnotationsRegistry;
import org.zhupanovdm.microbus.utils.CommonUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

@Slf4j
public class ActivationLauncher {
    public static final String ON_DISCOVER_METHOD = "onDiscover";

    private final AppContext context;
    private final Map<ActivatorTemplate<?>, Activator> metadata = new HashMap<>();
    private final Queue<ActivatorTemplate<?>> activators = new PriorityQueue<>(Comparator.comparingInt(a -> metadata.get(a).priority()));

    public ActivationLauncher(AppContext context) {
        this.context = context;
    }

    public void engage() {
        log.debug("Engaging activators");

        CommonUtils.visitRows(context.getAnnotationsRegistry().getClasses().scan(Activator.class), (type, table) -> createActivator(type, resolveKey(type, table)));

        while (activators.peek() != null) {
            ActivatorTemplate<?> activator = activators.poll();

            AnnotationsRegistry registry = context.getAnnotationsRegistry();
            discover(activator, Class.class, registry.getClasses());
            discover(activator, Constructor.class, registry.getConstructors());
            discover(activator, Method.class, registry.getMethods());
            discover(activator, Field.class, registry.getFields());
            discover(activator, Parameter.class, registry.getParameters());

            activator.activate();

            log.info("Activation complete: {}", activator.getClass().getCanonicalName());
        }
    }

    private void createActivator(Class<?> type, Activator annotation) {
        ActivatorTemplate<?> activator;
        try {
            activator = (ActivatorTemplate<?>) type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Failed to instantiate activator {}", type, e);
            throw new RuntimeException("Failed to instantiate activator " + type, e);
        }
        metadata.put(activator, annotation);
        activators.add(activator);
    }

    private static <R, C> C resolveKey(R key, Table<R, C, Integer> table) {
        return AnnotatedElementsHolder.withHighestPriorityResolved(key, table, collision -> {
            log.error("Ambiguous definitions for {}: {}", key, collision);
            throw new IllegalStateException("Ambiguous definitions for " + key + ": " + collision);
        }).orElseThrow(() -> {
            log.error("No definitions found for {}", key);
            return new NoSuchElementException("No definitions found for " + key);
        });
    }

    private <T extends AnnotatedElement> void discover(ActivatorTemplate<?> activator, Class<?> elementType, AnnotatedElementsHolder<T> annotated) {
        Class<? extends Annotation> marker = metadata.get(activator).marker();

        Method method;
        try {
            method = activator.getClass().getDeclaredMethod(ON_DISCOVER_METHOD, elementType, marker);
        } catch (NoSuchMethodException e) {
            return;
        }

        CommonUtils.visitRows(annotated.scan(marker), (element, table) -> {
            Annotation annotation = resolveKey(element, table);
            try {
                method.invoke(activator, element, annotation);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Failed invocation of activator method {}", method, e);
                throw new RuntimeException("Failed invocation of activator method: " + method, e);
            }
        });
    }

}
