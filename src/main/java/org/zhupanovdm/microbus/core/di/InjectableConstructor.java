package org.zhupanovdm.microbus.core.di;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class InjectableConstructor extends InjectableExecutable<Constructor<?>> {
    public InjectableConstructor(Constructor<?> executable, DependencyQualifierProvider qualifierProvider) {
        super(executable, qualifierProvider);
    }

    @Override
    protected Object doInvoke(Object target, Object[] args) {
        try {
            return executable.newInstance(args);
        } catch (IllegalAccessException e) {
            executable.setAccessible(true);
            Object instance = doInvoke(target, args);
            executable.setAccessible(false);
            return instance;
        } catch (InstantiationException | InvocationTargetException e) {
            log.error("Failed executable invocation {}", executable, e);
            throw new RuntimeException("Failed executable invocation: " + executable, e);
        }
    }
}
