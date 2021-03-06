package org.zhupanovdm.microbus.core.reflector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class AnnotationRegistry {
    private final Multimap<Class<? extends Annotation>, Class<? extends Annotation>> annotations = HashMultimap.create();

    @Getter
    private final AnnotatedElementHolder<Class<?>> classes = new AnnotatedElementHolder<>(annotations);

    @Getter
    private final AnnotatedElementHolder<Field> fields = new AnnotatedElementHolder<>(annotations);

    @Getter
    private final AnnotatedElementHolder<Constructor<?>> constructors = new AnnotatedElementHolder<>(annotations);

    @Getter
    private final AnnotatedElementHolder<Method> methods = new AnnotatedElementHolder<>(annotations);

    @Getter
    private final AnnotatedElementHolder<Parameter> parameters = new AnnotatedElementHolder<>(annotations);

    public void scan(Class<?> ...classes) {
        for (Class<?> aClass : classes) {
            if (aClass.isAnnotation())
                continue;

            registerElements(new Class[] { aClass }, this.classes);
            registerElements(aClass.getDeclaredFields(), fields);
            registerElements(aClass.getDeclaredConstructors(), constructors,
                    constructor -> registerElements(constructor.getParameters(), parameters));
            registerElements(aClass.getDeclaredMethods(), methods,
                    method -> registerElements(method.getParameters(), parameters));
        }
    }

    public Multimap<Class<? extends Annotation>, Class<? extends Annotation>> getAnnotations() {
        return Multimaps.unmodifiableMultimap(annotations);
    }

    private void registerAnnotationTypeHierarchy(Annotation annotation) {
        registerAnnotationTypeHierarchy(annotation.annotationType(), null, new LinkedHashSet<>());
    }

    private void registerAnnotationTypeHierarchy(Class<? extends Annotation> current, Class<? extends Annotation> child, LinkedHashSet<Class<? extends Annotation>> path) {
        checkCyclic(current, path);
        path.add(current);

        if (child != null)
            annotations.put(current, child);

        for (Annotation parent : current.getAnnotations()) {
            Class<? extends Annotation> type = parent.annotationType();
            if (acceptAnnotationType(type)) {
                registerAnnotationTypeHierarchy(type, current, path);
                path.remove(type);
            }
        }
    }

    private <T extends AnnotatedElement> void registerElements(T[] elements, AnnotatedElementHolder<T> holder) {
        registerElements(elements, holder, null);
    }

    private <T extends AnnotatedElement> void registerElements(T[] elements, AnnotatedElementHolder<T> holder, Consumer<T> consumer) {
        for (T element : elements) {
            for (Annotation annotation : element.getAnnotations()) {
                if (acceptAnnotationType(annotation.annotationType())) {
                    registerAnnotationTypeHierarchy(annotation);
                    holder.put(annotation.annotationType(), element);
                    log.trace("Found {} with {}", element, annotation);
                }
            }

            if (consumer != null)
                consumer.accept(element);
        }
    }

    private boolean acceptAnnotationType(Class<? extends Annotation> type) {
        String packageName = type.getPackageName();
        return !packageName.startsWith("java.lang.annotation") && !packageName.startsWith("javax.annotation");
    }

    private void checkCyclic(Class<? extends Annotation> current, LinkedHashSet<Class<? extends Annotation>> path) {
        if (!path.contains(current))
            return;

        StringBuilder annotationsChain = new StringBuilder(current.toString()).append('\n');
        StringBuilder offset = new StringBuilder();

        List<Class<? extends Annotation>> list = new ArrayList<>(path);
        for (int i = list.size() - 1; i >= 0; i--) {
            Class<? extends Annotation> annotationType = list.get(i);
            annotationsChain.append(offset.append(' '));
            annotationsChain.append(annotationType.toString());
            annotationsChain.append('\n');
            if (annotationType.equals(current))
                break;
        }

        log.error("Cyclic annotation detected: {} path:\n{}", current, annotationsChain);

        throw new IllegalStateException("Cyclic annotation " + current);
    }

}
