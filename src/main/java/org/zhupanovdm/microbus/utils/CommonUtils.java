package org.zhupanovdm.microbus.utils;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommonUtils {
    public static <R> void printTable(Table<R, ?, ?> table) {
        for (R rowKey : table.rowKeySet()) {
            System.out.println(rowKey);
            for (Map.Entry<?, ?> col : table.row(rowKey).entrySet()) {
                System.out.println(" + " + col.getKey() + " : " + col.getValue());
            }
            System.out.println();
        }
    }

    public static <K> void printMultimap(Multimap<K, ?> map) {
        for (K key : map.keySet()) {
            System.out.println(key);
            for (Object col : map.get(key)) {
                System.out.println(" + " + col);
            }
            System.out.println();
        }
    }

    public static <R, C> Optional<C> withHighestPriorityResolved(R key, Table<R, C, Integer> table, Function<Set<C>, Optional<C>> collisionResolver) {
        Set<C> result = withHighestPriority(key, table);
        return result.size() > 1 ? collisionResolver.apply(result) : anyOf(result);
    }

    public static <R, C> Set<C> withHighestPriority(R key, Table<R, C, Integer> table) {
        Map<C, Integer> row = table.row(key);
        int priority = row.values().stream().min(Integer::compare).orElse(-1);
        return row.entrySet().stream()
                .filter(entry -> entry.getValue().equals(priority))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static <T> Optional<T> anyOf(Iterable<T> iterable) {
        for (T element : iterable)
            return Optional.of(element);
        return Optional.empty();
    }

    public static <T> T doWithLock(Lock lock, Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    public static boolean isDefined(String value) {
        return value != null && !value.isBlank();
    }

}