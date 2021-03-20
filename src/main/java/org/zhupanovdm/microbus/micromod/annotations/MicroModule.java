package org.zhupanovdm.microbus.micromod.annotations;

import org.zhupanovdm.microbus.micromod.spawner.SpawnStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR })
public @interface MicroModule {
    String value() default "";
    Class<? extends SpawnStrategy> spawn() default SpawnStrategy.Singleton.class;
}
