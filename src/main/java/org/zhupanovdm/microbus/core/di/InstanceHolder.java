package org.zhupanovdm.microbus.core.di;

public interface InstanceHolder<T> {
    T getInstance();
    void setInstance(T instance);
}
