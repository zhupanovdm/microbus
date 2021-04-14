package org.zhupanovdm.microbus.core.activator;

import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.AppContext;
import org.zhupanovdm.microbus.core.reflector.AnnotatedElementHolder;
import org.zhupanovdm.microbus.core.reflector.AnnotationRegistry;
import org.zhupanovdm.microbus.core.reflector.ReflectorUtils;
import org.zhupanovdm.microbus.CommonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Slf4j
public class ActivatorLauncher {
    public static final String ON_DISCOVER_METHOD = "onDiscover";

    private final AppContext context;
    private final Pattern packagePattern;

    public ActivatorLauncher(@Nonnull AppContext context, @Nullable String packagePattern) {
        this.context = context;
        this.packagePattern = ReflectorUtils.packageFilterPattern(packagePattern);
    }

    public void engage() {
        log.debug("Engaging activators with package filter: {}", packagePattern);

        AnnotationRegistry annotationRegistry = context.getAnnotationRegistry();
        context.getActivatorRegistry().getAll().forEach(activator -> {
            discover(activator, Class.class, annotationRegistry.getClasses(), packageFilter(ReflectorUtils::getPackageName));
            discover(activator, Constructor.class, annotationRegistry.getConstructors(), packageFilter(ReflectorUtils::getPackageName));
            discover(activator, Method.class, annotationRegistry.getMethods(), packageFilter(ReflectorUtils::getPackageName));
            discover(activator, Field.class, annotationRegistry.getFields(), packageFilter(ReflectorUtils::getPackageName));
            discover(activator, Parameter.class, annotationRegistry.getParameters(), packageFilter(ReflectorUtils::getPackageName));
            activator.activate();

            log.info("Activation complete: {}", activator.getClass().getCanonicalName());
        });
    }

    private <T extends AnnotatedElement> void discover(ActivatorTemplate<?> activator, Class<?> elementType, AnnotatedElementHolder<T> annotated, Predicate<T> filter) {
        Class<? extends Annotation> marker = context.getActivatorRegistry().get(activator.getClass()).getMarker();
        Method method;
        try {
            method = activator.getClass().getDeclaredMethod(ON_DISCOVER_METHOD, elementType, marker);
        } catch (NoSuchMethodException e) {
            return;
        }

        CommonUtils.forEachRow(annotated.scan(marker, filter), (element, table) -> {
            Annotation annotation = AnnotatedElementHolder.getSingle(element, table);
            try {
                method.invoke(activator, element, annotation);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Failed invocation of activator method {}", method, e);
                throw new RuntimeException("Failed invocation of activator method: " + method, e);
            }
        });
    }

    private <T> Predicate<T> packageFilter(Function<T, String> mapper) {
        return val -> packagePattern == null || packagePattern.matcher(mapper.apply(val)).matches();
    }

}
