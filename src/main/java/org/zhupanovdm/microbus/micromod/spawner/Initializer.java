package org.zhupanovdm.microbus.micromod.spawner;

import org.zhupanovdm.microbus.micromod.Dependent;
import org.zhupanovdm.microbus.micromod.ModuleQuery;
import org.zhupanovdm.microbus.micromod.annotations.Inject;

import java.lang.reflect.Field;
import java.util.*;

public class Initializer implements Dependent {
    private final Class<?> clazz;
    private final Map<Field, Object> values = new HashMap<>();

    public Initializer(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Collection<ModuleQuery> getDependencies() {
        List<ModuleQuery> dependencies = new LinkedList<>();
        for (Field field : clazz.getDeclaredFields()) {
            Inject annotation = field.getAnnotation(Inject.class);
            if (annotation != null) {
                dependencies.add(ModuleQuery.of(field).withInjectAnnotationOf(field).withInjector(o -> values.put(field, o)));
            }
        }
        return dependencies;
    }

    public void init(Object obj) {
        values.keySet().forEach(field -> initField(obj, field));
    }

    private void initField(Object obj, Field field) {
        try {
            field.set(obj, values.get(field));
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            initField(obj, field);
            field.setAccessible(false);
        }
    }

}
