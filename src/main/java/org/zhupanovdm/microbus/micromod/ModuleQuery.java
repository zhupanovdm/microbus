package org.zhupanovdm.microbus.micromod;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.zhupanovdm.microbus.micromod.annotations.Inject;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Set;

@Getter
@ToString
@EqualsAndHashCode(of = { "id", "type", "strictId" })
public class ModuleQuery {
    private String id;
    private final Class<?> type;
    private boolean strictId;
    private Set<Module> chain;

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

    private ModuleQuery withInjectAnnotation(AnnotatedElement element) {
        Inject annotation = element.getAnnotation(Inject.class);
        if (annotation != null && !annotation.value().isBlank()) {
            id = annotation.value();
            strictId = true;
        }
        return this;
    }

    public ModuleQuery withChain(Set<Module> chain) {
        this.chain = chain;
        return this;
    }

    public static ModuleQuery create(String id) {
        return create(id, null);
    }

    public static ModuleQuery create(Class<?> type) {
        return create(null, type);
    }

    public static ModuleQuery create(String id, Class<?> type) {
        return create(id, type, false);
    }

    public static ModuleQuery create(String id, Class<?> type, boolean strictId) {
        return new ModuleQuery(id, type, strictId);
    }

    public static ModuleQuery create(@NonNull Field field) {
        return create(field.getName(), field.getType()).withInjectAnnotation(field);
    }

    public static ModuleQuery create(@NonNull Parameter parameter) {
        return create(parameter.getType()).withInjectAnnotation(parameter);
    }
}