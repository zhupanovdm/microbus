package org.zhupanovdm.microbus.core;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.activator.ActivatorTemplate;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.reflector.AnnotatedElementHolder;
import org.zhupanovdm.microbus.core.reflector.AnnotationRegistry;
import org.zhupanovdm.microbus.util.CommonUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.function.Predicate;

import static org.zhupanovdm.microbus.core.reflector.ReflectorUtils.getPackageName;

@Slf4j
public class ActivationLauncher {
    public static final String ON_DISCOVER_METHOD = "onDiscover";

    private final AnnotationRegistry annotationRegistry;
    private final ActivatorRegistry activatorRegistry;

    public ActivationLauncher(AnnotationRegistry annotationRegistry, ActivatorRegistry activatorRegistry) {
        this.annotationRegistry = annotationRegistry;
        this.activatorRegistry = activatorRegistry;
    }

    public ActivationLauncher scan() {
        CommonUtils.visitRows(annotationRegistry.getClasses().scan(Activator.class), (type, table) -> activatorRegistry.register(type, AnnotatedElementHolder.resolveElement(type, table)));
        return this;
    }

    public void engage() {
        engage(null);
    }

    public void engage(String packageRegex) {
        log.debug("Engaging activators with package filter: {}", packageRegex);

        activatorRegistry.collect().forEach(activator -> {
            discover(activator, Class.class, annotationRegistry.getClasses(), aClass -> packageRegex == null || getPackageName(aClass).matches(packageRegex));
            discover(activator, Constructor.class, annotationRegistry.getConstructors(), executable -> packageRegex == null || getPackageName(executable).matches(packageRegex));
            discover(activator, Method.class, annotationRegistry.getMethods(), executable -> packageRegex == null || getPackageName(executable).matches(packageRegex));
            discover(activator, Field.class, annotationRegistry.getFields(), field -> packageRegex == null || getPackageName(field).matches(packageRegex));
            discover(activator, Parameter.class, annotationRegistry.getParameters(), param -> packageRegex == null || getPackageName(param).matches(packageRegex));
            activator.activate();

            log.info("Activation complete: {}", activator.getClass().getCanonicalName());
        });
    }

    private <T extends AnnotatedElement> void discover(ActivatorTemplate<?> activator, Class<?> elementType, AnnotatedElementHolder<T> annotated, Predicate<T> filter) {
        Class<? extends Annotation> marker = activatorRegistry.getMetadata(activator.getClass()).marker();

        Method method;
        try {
            method = activator.getClass().getDeclaredMethod(ON_DISCOVER_METHOD, elementType, marker);
        } catch (NoSuchMethodException e) {
            return;
        }

        CommonUtils.visitRows(annotated.scan(marker, filter), (element, table) -> {
            Annotation annotation = AnnotatedElementHolder.resolveElement(element, table);
            try {
                method.invoke(activator, element, annotation);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Failed invocation of activator method {}", method, e);
                throw new RuntimeException("Failed invocation of activator method: " + method, e);
            }
        });
    }

}
