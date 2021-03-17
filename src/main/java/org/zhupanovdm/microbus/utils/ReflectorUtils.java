package org.zhupanovdm.microbus.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.zhupanovdm.microbus.micromod.reflector.AnnotatedElementsHolder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectorUtils {
    public static <T extends Member> Multimap<Class<?>, T> getDeclaringClass(Collection<T> members) {
        Multimap<Class<?>, T> map = HashMultimap.create();
        for (T member : members) {
            map.put(member.getDeclaringClass(), member);
        }
        return map;
    }

    public static <P extends Parameter> Multimap<Executable, P> getDeclaringExecutable(Collection<P> parameters) {
        Multimap<Executable, P> map = HashMultimap.create();
        for (P parameter : parameters) {
            map.put(parameter.getDeclaringExecutable(), parameter);
        }
        return map;
    }

    public static <R, C> Set<C> withHighestPriority(R key, Table<R, C, Integer> table) {
        Map<C, Integer> row = table.row(key);
        int priority = row.values().stream().min(Integer::compare).orElse(-1);
        return row.entrySet().stream()
                .filter(entry -> entry.getValue().equals(priority))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static void printAnnotationsTree(AnnotatedElementsHolder<Class<? extends Annotation>> holder) {
        Set<Class<? extends Annotation>> annotations = holder.annotations();
        var children = annotations.stream()
                .flatMap(aClass -> holder.get(aClass).stream())
                .collect(Collectors.toUnmodifiableSet());

        annotations.stream()
                .filter(aClass -> !children.contains(aClass))
                .collect(Collectors.toUnmodifiableSet())
                .forEach(aClass -> printAnnotationsTree(holder, aClass, "", 0));
    }

    private static void printAnnotationsTree(AnnotatedElementsHolder<Class<? extends Annotation>> holder, Class<? extends Annotation> current, String offset, int i) {
        System.out.println(offset + " " + i + ": " + current);
        for (var a : holder.get(current)) {
            printAnnotationsTree(holder, a, offset + ' ', i + 1);
        }
    }

}
