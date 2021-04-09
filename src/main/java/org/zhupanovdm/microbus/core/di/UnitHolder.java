package org.zhupanovdm.microbus.core.di;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.AppContext;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@EqualsAndHashCode(of = { "id" })
public class UnitHolder {
    @Getter
    private final String id;

    @Getter
    private final String name;

    @Getter
    private final Class<?> type;

    protected final InjectableExecutable<?> constructor;
    protected final AppContext context;

    private final CreationStrategy strategy;

    public UnitHolder(String id, Class<?> type, InjectableExecutable<?> constructor, String name, Class<? extends CreationStrategy> creationStrategy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.constructor = constructor;
        this.strategy = CreationStrategy.create(creationStrategy);
        this.context = App.getContext();
    }

    public Object resolve() {
        return resolve(new LinkedHashSet<>());
    }

    private Object resolve(Set<UnitHolder> chain) {
        if (!chain.add(this)) {
            log.error("Can not resolve recursive dependency for {} previous resolves: {}", this, chain);
            throw new IllegalStateException("Can not resolve recursive dependency");
        }

        Function<UnitQuery, ?> injector = query -> requestDependency(query).resolve(chain);

        ObjectInitializer initializer = context.getObjectInitializer();
        Object instance = strategy.getInstance(() -> {
            Object o = constructor.invoke(injector);
            initializer.init(o, injector);
            log.trace("Created {}", o);
            return o;
        });
        chain.remove(this);

        return instance;
    }

    private UnitHolder requestDependency(UnitQuery query) {
        return context.getUnitRegistry().request(query).orElseThrow(() -> {
            log.error("Failed to satisfy unit dependency with query {}", query);
            return new NoSuchElementException("No unit found on specified request");
        });
    }

    public String toString() {
        return "UNIT@" + id;
    }

}