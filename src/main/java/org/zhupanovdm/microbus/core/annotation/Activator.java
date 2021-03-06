package org.zhupanovdm.microbus.core.annotation;

import org.zhupanovdm.microbus.core.di.CreationStrategy;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
public @interface Activator {
    Class<? extends Annotation> marker();
    int priority() default Integer.MAX_VALUE;
    Class<? extends CreationStrategy> strategy() default CreationStrategy.Singleton.class;
}
