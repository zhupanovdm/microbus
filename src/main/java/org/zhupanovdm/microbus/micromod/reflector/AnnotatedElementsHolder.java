package org.zhupanovdm.microbus.micromod.reflector;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class AnnotatedElementsHolder<T extends AnnotatedElement> {
    private final AnnotationsRegistry registry;
    private final Multimap<Class<? extends Annotation>, T> annotated = HashMultimap.create();

    public AnnotatedElementsHolder(AnnotationsRegistry registry) {
        this.registry = registry;
    }

    public void put(Class<? extends Annotation> type, T element) {
        annotated.put(type, element);
    }

    public Collection<T> get(Class<? extends Annotation> type) {
        return Collections.unmodifiableCollection(annotated.get(type));
    }

    public Set<Class<? extends Annotation>> annotations() {
        return Collections.unmodifiableSet(annotated.keySet());
    }

    public <A extends Annotation> Table<T, A, Integer> scan(Class<A> type) {
        return scan(type, type, null, 0, HashBasedTable.create());
    }

    private <A extends Annotation> Table<T, A, Integer> scan(Class<? extends Annotation> currentType, Class<A> requestedType, A annotation, int priority, Table<T, A, Integer> result) {
        for (T element : annotated.get(currentType)) {
            A actualAnnotation = annotation == null ? element.getAnnotation(requestedType) : annotation;
            if (!result.contains(element, actualAnnotation)) {
                result.put(element, actualAnnotation, priority);
            }
        }
        for (Class<? extends Annotation> child : registry.getAnnotations().get(currentType)) {
            scan(child, requestedType, annotation == null ? child.getAnnotation(requestedType) : annotation, priority + 1, result);
        }
        return result;
    }

}
