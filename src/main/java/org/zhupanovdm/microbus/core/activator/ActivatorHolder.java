package org.zhupanovdm.microbus.core.activator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.zhupanovdm.microbus.core.annotation.Activator;
import org.zhupanovdm.microbus.core.di.CreationStrategy;
import org.zhupanovdm.microbus.core.di.InstanceHolder;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.annotation.Annotation;

@Data
@NotThreadSafe
@ToString(of = { "type" }, includeFieldNames = false)
@EqualsAndHashCode(of = { "type" })
public class ActivatorHolder implements InstanceHolder<ActivatorTemplate<?>> {
    private final Class<?> type;
    private final int priority;
    private final Class<? extends Annotation> marker;
    private final Class<? extends CreationStrategy> strategy;
    private ActivatorTemplate<?> instance;

    public ActivatorHolder(Class<?> type, Activator annotation) {
        this.type = type;
        this.priority = annotation.priority();
        this.marker = annotation.marker();
        this.strategy = annotation.strategy();
    }

    @Override
    public ActivatorTemplate<?> getInstance() {
        return instance;
    }

    @Override
    public void setInstance(ActivatorTemplate<?> instance) {
        this.instance = instance;
    }

}
