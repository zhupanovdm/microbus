package org.zhupanovdm.microbus.core.di.test.samples.sample04;

import org.zhupanovdm.microbus.core.annotation.Id;
import org.zhupanovdm.microbus.core.annotation.Unit;
import org.zhupanovdm.microbus.core.di.CreationStrategy;

@Unit(strategy = CreationStrategy.Factory.class)
@Id("unit-dep-2")
public class UnitDependency02 implements IDependency {
}
