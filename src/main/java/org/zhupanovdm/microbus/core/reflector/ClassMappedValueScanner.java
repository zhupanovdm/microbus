package org.zhupanovdm.microbus.core.reflector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@NotThreadSafe
public class ClassMappedValueScanner<T> {
    private final Function<T, Class<?>> valueToClass;
    private final Multimap<Class<?>, T> values = HashMultimap.create();
    private final Multimap<Class<?>, Class<?>> hierarchy = HashMultimap.create();

    public ClassMappedValueScanner(Function<T, Class<?>> valueToClass) {
        this.valueToClass = valueToClass;
    }

    public void put(T value) {
        values.put(valueToClass.apply(value), value);
        scanAncestors(valueToClass.apply(value), hierarchy::put);
    }

    public void remove(T value) {
        values.remove(valueToClass.apply(value), value);
        rebuildHierarchy();
    }

    public void remove(Iterable<T> iterable) {
        iterable.forEach(value -> values.remove(valueToClass.apply(value), value));
        rebuildHierarchy();
    }

    public Set<Class<?>> types() {
        return Collections.unmodifiableSet(values.keySet());
    }

    public <U extends Collection<T>> U collect(Class<?> clazz, U collection) {
        scan(clazz, (modules, integer) -> {
            collection.addAll(modules);
            return true;
        }, 0);
        return collection;
    }

    public Collection<T> get(Class<?> clazz) {
        return values.get(clazz);
    }

    public void scan(Class<?> clazz, BiFunction<Collection<T>, Integer, Boolean> visitor) {
        scan(clazz, visitor, 0);
    }

    private void scan(Class<?> clazz, BiFunction<Collection<T>, Integer, Boolean> visitor, int generation) {
        if (clazz == null)
            return;

        Collection<T> result = values.get(clazz);
        if (!result.isEmpty() && !visitor.apply(Collections.unmodifiableCollection(result), generation))
            return;

        hierarchy.get(clazz).forEach(subclass -> scan(subclass, visitor, generation + 1));
    }

    private void rebuildHierarchy() {
        hierarchy.clear();
        values.keySet().forEach(aClass -> scanAncestors(aClass, hierarchy::put));
    }

    private static void scanAncestors(Class<?> clazz, BiFunction<Class<?>, Class<?>, Boolean> visitor) {
        scanInterfaces(clazz, visitor);
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            if (visitor.apply(superclass, clazz))
                scanAncestors(superclass, visitor);
        }
    }

    private static void scanInterfaces(Class<?> clazz, BiFunction<Class<?>, Class<?>, Boolean> visitor) {
        for(Class<?> iFace :clazz.getInterfaces()) {
            if (visitor.apply(iFace, clazz))
                scanInterfaces(iFace, visitor);
        }
    }
}
