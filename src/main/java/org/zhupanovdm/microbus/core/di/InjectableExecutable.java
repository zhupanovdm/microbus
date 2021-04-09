package org.zhupanovdm.microbus.core.di;

import lombok.Getter;
import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.AppContext;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.Function;

public abstract class InjectableExecutable<T extends Executable> {
    @Getter
    protected final T executable;
    protected final AppContext context;

    public InjectableExecutable(T executable, AppContext context) {
        this.executable = executable;
        this.context = context;
    }

    public InjectableExecutable(T executable) {
        this(executable, App.getContext());
    }

    public Object invoke(Function<UnitQuery, ?> injector) {
        return doInvoke(getTarget(injector), getArgs(injector));
    }

    protected Object[] getArgs(Function<UnitQuery, ?> injector) {
        DependencyQualifier<Parameter> argumentQualifier = context.getArgumentQualifier();
        return Arrays.stream(executable.getParameters())
                .map(argumentQualifier::toQuery)
                .map(injector)
                .toArray();
    }

    protected Object getTarget(Function<UnitQuery, ?> injector) {
        UnitQuery query = context.getExecutableTargetQualifier().toQuery(executable);
        return query == null ? null : injector.apply(query);
    }

    protected abstract Object doInvoke(Object target, Object[] args);

}
