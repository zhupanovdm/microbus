package org.zhupanovdm.microbus.core.di.test.samples.sample03;

import org.zhupanovdm.microbus.core.annotation.Inject;
import org.zhupanovdm.microbus.core.annotation.Unit;

@Unit
public class DependentUnit {
    public DependencyIFace d01;

    @Inject("dependency02")
    private DependencyIFace d02;

    @Inject
    public DependencyIFace dependency03;

    public DependentUnit(@Inject("dependency01") DependencyIFace d01) {
        this.d01 = d01;
    }

    public DependencyIFace getD02() {
        return d02;
    }
}
