package org.zhupanovdm.microbus.core.di.test.samples.sample02;

import org.zhupanovdm.microbus.core.annotation.Unit;

@Unit
public class RootUnit {
    public final Dependency1 dependency1;
    public final IFace dependency2;

    public RootUnit(Dependency1 a, IFace b) {
        this.dependency1 = a;
        this.dependency2 = b;
    }

}
