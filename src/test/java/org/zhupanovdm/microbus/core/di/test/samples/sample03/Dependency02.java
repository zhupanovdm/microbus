package org.zhupanovdm.microbus.core.di.test.samples.sample03;

import org.zhupanovdm.microbus.core.annotation.Inject;
import org.zhupanovdm.microbus.core.annotation.Unit;

@Unit
public class Dependency02 implements DependencyIFace {
    @Inject
    public Dependency03 dependency03;
}
