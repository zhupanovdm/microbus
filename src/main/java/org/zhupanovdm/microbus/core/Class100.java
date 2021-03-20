package org.zhupanovdm.microbus.core;

import org.zhupanovdm.microbus.micromod.annotations.MicroModule;

@MicroModule
public class Class100 {
    Class100(Class121 class121, Class110 class110) {
        System.out.println("this: " + this);
        System.out.println("dep class121" + class121);
        System.out.println("dep class110" + class110);
    }
}
