package org.zhupanovdm.microbus.micromod;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.zhupanovdm.microbus.micromod.annotations.Inject;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.function.Consumer;

@Data
@ToString
@EqualsAndHashCode(of = { "id", "type", "strictId" })
public class ModuleQuery {
    private String id;
    private final Class<?> type;
    private boolean strictId;

    private Consumer<Object> injector;

    private ModuleQuery(String id, Class<?> type, boolean strictId) {
        this.id = id;
        this.type = type;
        this.strictId = strictId;
    }

    public boolean isTypeCompatibleWith(@NonNull Module module) {
        return type == null || type.isAssignableFrom(module.getType());
    }

    public boolean hasId() {
        return id != null && !id.isBlank();
    }

    public ModuleQuery withInjectAnnotationOf(AnnotatedElement element) {
        Inject annotation = element.getAnnotation(Inject.class);
        if (annotation != null && !annotation.value().isBlank()) {
            id = annotation.value();
            strictId = true;
        }
        return this;
    }

    public ModuleQuery withInjector(Consumer<Object> injector) {
        this.injector = injector;
        return this;
    }

    public static ModuleQuery of(String id) {
        return of(id, null);
    }

    public static ModuleQuery of(Class<?> type) {
        return of(null, type);
    }

    public static ModuleQuery of(String id, Class<?> type) {
        return of(id, type, false);
    }

    public static ModuleQuery of(String id, Class<?> type, boolean strictId) {
        return new ModuleQuery(id, type, strictId);
    }

    public static ModuleQuery of(@NonNull Field field) {
        return of(field.getName(), field.getType());
    }

    public static ModuleQuery of(@NonNull Parameter parameter) {
        return of(parameter.getType());
    }

    public static ModuleQuery inject(Parameter parameter, Consumer<Object> injector) {
        return of(parameter)
                .withInjectAnnotationOf(parameter)
                .withInjector(injector);
    }

}