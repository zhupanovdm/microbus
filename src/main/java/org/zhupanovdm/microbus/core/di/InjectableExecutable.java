package org.zhupanovdm.microbus.core.di;

import lombok.Getter;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.function.Function;

public abstract class InjectableExecutable<T extends Executable> {
    @Getter
    protected final T executable;
    protected final DependencyQualifierProvider qualifierProvider;

    public InjectableExecutable(T executable, DependencyQualifierProvider qualifierProvider) {
        this.executable = executable;
        this.qualifierProvider = qualifierProvider;
    }

    public Object invoke(Function<UnitQuery, ?> injector) {
        return doInvoke(getTarget(injector), getArgs(injector));
    }

    protected Object[] getArgs(Function<UnitQuery, ?> injector) {
        return Arrays.stream(executable.getParameters())
                .map(qualifierProvider::qualify)
                .map(injector)
                .toArray();
    }

    protected Object getTarget(Function<UnitQuery, ?> injector) {
        UnitQuery query = qualifierProvider.qualify(executable);
        return query == null ? null : injector.apply(query);
    }

    protected abstract Object doInvoke(Object target, Object[] args);

}
