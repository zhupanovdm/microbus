package org.zhupanovdm.microbus.core.di.test.samples.sample04;

import org.zhupanovdm.microbus.core.annotation.Inject;
import org.zhupanovdm.microbus.core.annotation.Unit;
import org.zhupanovdm.microbus.core.di.CreationStrategy;

@Unit
public class Factory {

    @Inject("unit-dep-1")
    private IDependency dependency;

    @Unit
    public Unit01 unitFactory01(@Inject("unit-dep-1") IDependency unit) {
        return new Unit01(unit);
    }

    @Unit(strategy = CreationStrategy.Factory.class)
    public Unit01 unitFactory02(@Inject("unit-dep-2") IDependency unit) {
        return new Unit01(unit);
    }

    @Unit
    public Unit01 unitFactory03() {
        return new Unit01(dependency);
    }

}
