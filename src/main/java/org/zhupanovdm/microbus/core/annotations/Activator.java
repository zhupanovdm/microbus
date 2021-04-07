package org.zhupanovdm.microbus.core.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
public @interface Activator {
    Class<? extends Annotation> marker();
    int priority() default Integer.MAX_VALUE;
}
