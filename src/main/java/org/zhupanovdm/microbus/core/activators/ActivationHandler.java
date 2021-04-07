package org.zhupanovdm.microbus.core.activators;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@SuppressWarnings("unused")
public interface ActivationHandler<T extends Annotation> {
    default void activate() { }
    default void onDiscover(Class<?> aClass, T metadata) { }
    default void onDiscover(Constructor<?> constructor, T metadata) { }
    default void onDiscover(Method method, T metadata) { }
    default void onDiscover(Field field, T metadata) { }
    default void onDiscover(Parameter parameter, T metadata) { }
}
