package org.zhupanovdm.microbus.core.reflector;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.utils.CommonUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

@Slf4j
public class AnnotatedElementsHolder<T extends AnnotatedElement> {
    private final Multimap<Class<? extends Annotation>, Class<? extends Annotation>> annotations;
    private final Multimap<Class<? extends Annotation>, T> annotated = HashMultimap.create();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public AnnotatedElementsHolder(Multimap<Class<? extends Annotation>, Class<? extends Annotation>> annotations) {
        this.annotations = annotations;
    }

    public void put(Class<? extends Annotation> type, T element) {
        CommonUtils.doWithLock(lock.writeLock(), () -> annotated.put(type, element));
        log.trace("Found annotated {} with @{}", element, type.getCanonicalName());
    }

    public Collection<T> get(Class<? extends Annotation> type) {
        return CommonUtils.doWithLock(lock.readLock(), () -> Collections.unmodifiableCollection(annotated.get(type)));
    }

    public Set<Class<? extends Annotation>> annotations() {
        return CommonUtils.doWithLock(lock.readLock(), () -> Collections.unmodifiableSet(annotated.keySet()));
    }

    public <A extends Annotation> Table<T, A, Integer> scan(Class<A> type) {
        return scan(type, type, null, 0, HashBasedTable.create());
    }

    public <A extends Annotation> void scan(Class<A> type, BiConsumer<T, Table<T, A, Integer>> visitor) {
        CommonUtils.doWithLock(lock.readLock(), () -> {
            Table<T, A, Integer> table = scan(type);
            table.rowKeySet().forEach(key -> visitor.accept(key, table));
            return null;
        });
    }

    private <A extends Annotation> Table<T, A, Integer> scan(Class<? extends Annotation> currentType, Class<A> requestedType, A annotation, int priority, Table<T, A, Integer> result) {
        for (T element : annotated.get(currentType)) {
            A actualAnnotation = annotation == null ? element.getAnnotation(requestedType) : annotation;
            if (!result.contains(element, actualAnnotation)) {
                result.put(element, actualAnnotation, priority);
            }
        }
        for (Class<? extends Annotation> child : annotations.get(currentType)) {
            scan(child, requestedType, annotation == null ? child.getAnnotation(requestedType) : annotation, priority + 1, result);
        }
        return result;
    }

}
