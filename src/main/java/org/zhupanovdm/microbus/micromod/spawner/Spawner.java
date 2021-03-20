package org.zhupanovdm.microbus.micromod.spawner;

import org.zhupanovdm.microbus.micromod.Dependent;

import java.util.function.Supplier;

public interface Spawner extends Dependent, Supplier<Object> {
}