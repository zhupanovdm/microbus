package org.zhupanovdm.microbus.core.unit;

import org.zhupanovdm.microbus.core.annotations.Id;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

public class UnitUtils {
    public static String definedId(Class<?> aClass) {
        return getIdDefinition(aClass).orElseGet(() -> {
            String simpleName = aClass.getSimpleName();
            return simpleName.substring(0, 1).toLowerCase(Locale.ROOT) + simpleName.substring(1);
        });
    }

    public static String definedId(Method method) {
        return getIdDefinition(method).orElseGet(method::getName);
    }

    public static String name(Class<?> aClass) {
        return aClass.getCanonicalName();
    }

    public static String name(Method method) {
        return method.toGenericString();
    }

    private static Optional<String> getIdDefinition(AnnotatedElement element) {
        Id idAnnotation = element.getAnnotation(Id.class);
        if (idAnnotation != null) {
            String id = idAnnotation.value();
            if (id.isBlank())
                throw new IllegalArgumentException("Id value is blank: " + idAnnotation);
            return Optional.of(id);
        }
        return Optional.empty();
    }

}
