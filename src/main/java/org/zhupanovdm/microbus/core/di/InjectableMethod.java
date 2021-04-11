package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class InjectableMethod extends InjectableExecutable<Method> {
    public InjectableMethod(Method executable, DependencyQualifierProvider qualifierProvider) {
        super(executable, qualifierProvider);
    }

    @Override
    protected Object doInvoke(Object target, Object[] args) {
        try {
            return executable.invoke(target, args);
        } catch (IllegalAccessException e) {
            executable.setAccessible(true);
            Object instance = doInvoke(target, args);
            executable.setAccessible(false);
            return instance;
        } catch (InvocationTargetException e) {
            log.error("Failed executable invocation {}", executable, e);
            throw new RuntimeException("Failed executable invocation: " + executable, e);
        }
    }
}