package org.zhupanovdm.microbus.micromod.reflector;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
public class AnnotationsRegistry {
    private final AnnotatedElementsHolder<Class<? extends Annotation>> annotations = new AnnotatedElementsHolder<>(this);
    private final AnnotatedElementsHolder<Class<?>> classes = new AnnotatedElementsHolder<>(this);
    private final AnnotatedElementsHolder<Field> fields = new AnnotatedElementsHolder<>(this);
    private final AnnotatedElementsHolder<Constructor<?>> constructors = new AnnotatedElementsHolder<>(this);
    private final AnnotatedElementsHolder<Method> methods = new AnnotatedElementsHolder<>(this);
    private final AnnotatedElementsHolder<Parameter> parameters = new AnnotatedElementsHolder<>(this);

    public void scan(Class<?> ...classes) {
        for (Class<?> aClass : classes) {
            if (aClass.isAnnotation())
                continue;
            registerElements(new Class[] { aClass }, this.classes);
            registerElements(aClass.getFields(), fields);
            registerElements(aClass.getDeclaredConstructors(), constructors,
                    constructor -> registerElements(constructor.getParameters(), parameters));
            registerElements(aClass.getDeclaredMethods(), methods,
                    method -> registerElements(method.getParameters(), parameters));
        }
    }

    private void registerAnnotationTypeHierarchy(Annotation annotation) {
        registerAnnotationTypeHierarchy(annotation.annotationType(), null, new LinkedHashSet<>());
    }

    private void registerAnnotationTypeHierarchy(Class<? extends Annotation> current, Class<? extends Annotation> child, LinkedHashSet<Class<? extends Annotation>> path) {
        checkCyclic(current, path);
        path.add(current);

        if (child != null)
            annotations.put(current, child);

        List<Class<? extends Annotation>> ancestors = Arrays.stream(current.getAnnotations())
                .map(Annotation::annotationType)
                .filter(type -> !"java.lang.annotation".equals(type.getPackageName()))
                .collect(Collectors.toUnmodifiableList());

        for (Class<? extends Annotation> parent : ancestors) {
            registerAnnotationTypeHierarchy(parent, current, path);
            path.remove(parent);
        }
    }

    private <T extends AnnotatedElement> void registerElements(T[] elements, AnnotatedElementsHolder<T> holder) {
        registerElements(elements, holder, null);
    }

    private <T extends AnnotatedElement> void registerElements(T[] elements, AnnotatedElementsHolder<T> holder, Consumer<T> consumer) {
        for (T element : elements) {
            for (Annotation annotation : element.getAnnotations()) {
                registerAnnotationTypeHierarchy(annotation);
                holder.put(annotation.annotationType(), element);
            }
            if (consumer != null)
                consumer.accept(element);
        }
    }

    private void checkCyclic(Class<? extends Annotation> current, LinkedHashSet<Class<? extends Annotation>> path) {
        if (!path.contains(current))
            return;

        var list = new LinkedList<>(path);
        Collections.reverse(list);

        System.out.println(current);
        StringBuilder offset = new StringBuilder(" ");
        for (var type : list) {
            System.out.println(offset.toString() + type);
            if (type.equals(current))
                break;
            offset.append(' ');
        }
        throw new IllegalStateException("Cyclic annotation " + current);
    }

}
