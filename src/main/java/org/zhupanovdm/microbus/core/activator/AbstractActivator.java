package org.zhupanovdm.microbus.core.activator;

import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.AppContext;

import java.lang.annotation.Annotation;

public abstract class AbstractActivator<T extends Annotation> implements ActivatorTemplate<T> {
    protected final AppContext context;

    public AbstractActivator(AppContext context) {
        this.context = context;
    }

    public AbstractActivator() {
        this(App.getContext());
    }

}
