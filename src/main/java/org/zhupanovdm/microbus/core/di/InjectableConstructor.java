package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Slf4j
@ThreadSafe
public class InjectableConstructor extends InjectableExecutable<Constructor<?>> {
    public InjectableConstructor(Constructor<?> executable, DependencyQualifierProvider qualifierProvider) {
        super(executable, qualifierProvider);
    }

    @Override
    protected Object doInvoke(Object target, Object[] args) {
        log.trace("Invoking: {} with args: {} on target: {}", executable, args, target);
        try {
            return executable.newInstance(args);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            log.error("Failed executable invocation {}", executable, e);
            throw new RuntimeException("Failed executable invocation: " + executable, e);
        }
    }

    @Override
    protected boolean usesTarget() {
        return false;
    }
}
