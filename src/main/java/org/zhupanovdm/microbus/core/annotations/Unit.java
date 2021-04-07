package org.zhupanovdm.microbus.core.annotations;

import org.zhupanovdm.microbus.core.unit.CreationStrategy;
import org.zhupanovdm.microbus.core.unit.CreationStrategy.Singleton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface Unit {
    Class<? extends CreationStrategy> strategy() default Singleton.class;
}