package org.zhupanovdm.microbus.core.di;

import java.util.Set;
import java.util.function.Function;

public interface DependencyQualifier<T> {
    void setDefault(Function<T, UnitQuery> mapper);
    void define(T dependent, UnitQuery query);
    UnitQuery qualify(T dependent);
    Set<T> getAll();
}
