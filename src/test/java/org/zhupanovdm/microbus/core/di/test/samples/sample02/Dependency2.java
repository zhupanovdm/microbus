package org.zhupanovdm.microbus.core.di.test.samples.sample02;

import org.zhupanovdm.microbus.core.annotation.Unit;

@Unit
public class Dependency2 implements IFace {
    public final ChildDependency childDependency;

    public Dependency2(ChildDependency c) {
        this.childDependency = c;
    }
}
