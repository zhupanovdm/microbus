package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@ThreadSafe
public class InjectableMethod extends InjectableExecutable<Method> {
    public InjectableMethod(Method executable, DependencyQualifierProvider qualifierProvider) {
        super(executable, qualifierProvider);
    }

    @Override
    protected Object doInvoke(Object target, Object[] args) {
        log.trace("Invoking: {} with args: {} on target: {}", executable, args, target);
        try {
            return executable.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Failed executable invocation {}", executable, e);
            throw new RuntimeException("Failed executable invocation: " + executable, e);
        }
    }
}
