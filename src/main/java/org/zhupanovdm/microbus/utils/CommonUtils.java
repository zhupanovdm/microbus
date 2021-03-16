package org.zhupanovdm.microbus.utils;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import java.util.Map;

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

    public static <T> T anyOf(Iterable<T> collection) {
        for (T element : collection)
            return element;
        return null;
    }

    public static <T> T anyOf(T[] collection) {
        for (T element : collection)
            return element;
        return null;
    }

}