package org.zhupanovdm.microbus.core.reflector;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.util.CommonUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.zhupanovdm.microbus.util.CommonUtils.doWithLock;

@Slf4j
@ThreadSafe
public class AnnotatedElementHolder<T extends AnnotatedElement> {
    private final Multimap<Class<? extends Annotation>, Class<? extends Annotation>> annotations;
    private final Multimap<Class<? extends Annotation>, T> annotated = HashMultimap.create();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public AnnotatedElementHolder(Multimap<Class<? extends Annotation>, Class<? extends Annotation>> annotations) {
        this.annotations = annotations;
    }

    public void put(Class<? extends Annotation> type, T element) {
        doWithLock(lock.writeLock(), () -> annotated.put(type, element));
    }

    public Collection<T> get(Class<? extends Annotation> type) {
        return doWithLock(lock.readLock(), () -> Collections.unmodifiableCollection(annotated.get(type)));
    }

    public Set<Class<? extends Annotation>> annotations() {
        return doWithLock(lock.readLock(), () -> Collections.unmodifiableSet(annotated.keySet()));
    }

    public <A extends Annotation> Table<T, A, Integer> scan(Class<A> type) {
        return scan(type, null);
    }

    public <A extends Annotation> Table<T, A, Integer> scan(Class<A> type, Predicate<T> filter) {
        return doWithLock(lock.readLock(), () -> scan(type, type, null, 0, filter, HashBasedTable.create()));
    }

    private <A extends Annotation> Table<T, A, Integer> scan(Class<? extends Annotation> currentType, Class<A> requestedType, A annotation, int priority, Predicate<T> filter, Table<T, A, Integer> result) {
        for (T element : annotated.get(currentType)) {
            A actualAnnotation = annotation == null ? element.getAnnotation(requestedType) : annotation;
            if (!result.contains(element, actualAnnotation) && matchesPredicate(element, filter)) {
                result.put(element, actualAnnotation, priority);
            }
        }
        for (Class<? extends Annotation> child : annotations.get(currentType)) {
            scan(child, requestedType, annotation == null ? child.getAnnotation(requestedType) : annotation, priority + 1, filter, result);
        }
        return result;
    }

    private boolean matchesPredicate(T element, Predicate<T> predicate) {
        return predicate == null || predicate.test(element);
    }

    public static <R, C> Optional<C> withHighestPriorityResolved(R key, Table<R, C, Integer> table, Function<Set<C>, Optional<C>> collisionResolver) {
        Set<C> result = withHighestPriority(key, table);
        return result.size() > 1 ? collisionResolver.apply(result) : CommonUtils.anyOf(result);
    }

    public static <R, C> Set<C> withHighestPriority(R key, Table<R, C, Integer> table) {
        Map<C, Integer> row = table.row(key);
        int priority = row.values().stream().min(Integer::compare).orElse(-1);
        return row.entrySet().stream()
                .filter(entry -> entry.getValue().equals(priority))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static <R, C> C resolveElement(R element, Table<R, C, Integer> table) {
        return withHighestPriorityResolved(element, table, collision -> {
            log.error("Ambiguous definitions for {}: {}", element, collision);
            throw new IllegalStateException("Ambiguous definitions for " + element + ": " + collision);
        }).orElseThrow(() -> {
            log.error("No definitions found for {}", element);
            return new NoSuchElementException("No definitions found for " + element);
        });
    }

}
