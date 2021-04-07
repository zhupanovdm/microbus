package org.zhupanovdm.microbus.core.unit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.App;
import org.zhupanovdm.microbus.core.AppContext;
import org.zhupanovdm.microbus.core.components.ObjectInitializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import static org.zhupanovdm.microbus.core.unit.UnitQuery.Option.EXACT_TYPE;

@Slf4j
@Data
@EqualsAndHashCode(of = { "id" })
public abstract class UnitHolder<T extends Executable> {
    private final String id;
    private final String name;
    protected final T constructor;
    private final Class<?> type;
    private final CreationStrategy strategy;
    private final AppContext context;

    public UnitHolder(String id, Class<?> type, T constructor, String name, Class<? extends CreationStrategy> providerType) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.constructor = constructor;
        this.strategy = CreationStrategy.create(providerType);
        this.context = App.getContext();
    }

    public Object resolve() {
        return resolve(new LinkedHashSet<>());
    }

    private Object resolve(Set<UnitHolder<?>> chain) {
        if (!chain.add(this)) {
            log.error("Can not resolve recursive dependency for {} previous resolves: {}", this, chain);
            throw new IllegalStateException("Can not resolve recursive dependency");
        }

        Function<UnitQuery, ?> injector = query -> requestDependency(query).resolve(chain);

        ObjectInitializer initializer = context.getObjectInitializer();
        Object instance = strategy.getInstance(() -> create(injector), initializer::init);
        chain.remove(this);

        log.trace("Resolved {} to {}", this, instance);
        return instance;
    }

    private UnitHolder<?> requestDependency(UnitQuery query) {
        return context.getUnitRegistry().request(query).orElseThrow(() -> {
            log.error("Failed to query unit {}", query);
            return new NoSuchElementException("No unit found on specified request");
        });
    }

    protected abstract Object create(Function<UnitQuery, ?> injector);

    protected Object[] getArgs(Function<UnitQuery, ?> injector) {
        return Arrays.stream(constructor.getParameters())
                .map(parameter -> context.getArgumentQualifier().toQuery(parameter))
                .map(injector)
                .toArray();
    }

    public String toString() {
        return "UNIT:" + id;
    }

    public static class Constructable extends UnitHolder<Constructor<?>> {
        public Constructable(String id, Class<?> clazz, Class<? extends CreationStrategy> providerType) {
            super(id, clazz, constructor(clazz), UnitUtils.name(clazz), providerType);
        }

        @Override
        public Object create(Function<UnitQuery, ?> injector) {
            try {
                return constructor.newInstance(getArgs(injector));
            } catch (IllegalAccessException e) {
                constructor.setAccessible(true);
                Object instance = create(injector);
                constructor.setAccessible(false);
                return instance;
            } catch (InstantiationException | InvocationTargetException e) {
                log.error("Failed to spawn mod instance from constructor {}", constructor, e);
                throw new RuntimeException("Failed constructor invocation", e);
            }
        }

        private static Constructor<?> constructor(Class<?> clazz) {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            if (constructors.length != 1) {
                log.error("Only one constructor is supported for mod instantiation");
                throw new IllegalArgumentException("Only one constructor is supported: " + clazz);
            }
            return constructors[0];
        }

    }

    public static class Invokable extends UnitHolder<Method> {
        public Invokable(String id, Method method, Class<? extends CreationStrategy> providerType) {
            super(id, method.getReturnType(), method, UnitUtils.name(method), providerType);
        }

        @Override
        public Object create(Function<UnitQuery, ?> injector) {
            Class<?> declaring = constructor.getDeclaringClass();
            Object target = injector.apply(new UnitQuery(null, declaring, UnitUtils.name(declaring), EXACT_TYPE));
            try {
                return constructor.invoke(target, getArgs(injector));
            } catch (IllegalAccessException e) {
                constructor.setAccessible(true);
                Object instance = create(injector);
                constructor.setAccessible(false);
                return instance;
            } catch (InvocationTargetException e) {
                log.error("Failed to spawn mod instance from method {}", constructor, e);
                throw new RuntimeException("Failed method invocation", e);
            }
        }
    }

}