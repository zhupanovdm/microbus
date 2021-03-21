package org.zhupanovdm.microbus.micromod.spawner;

import com.google.common.collect.Lists;
import lombok.Data;
import org.zhupanovdm.microbus.micromod.Dependent;
import org.zhupanovdm.microbus.micromod.ModuleQuery;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Data
public abstract class Spawner implements Dependent, Function<Consumer<Collection<ModuleQuery>>, Object> {
    private final Initializer initializer;

    Spawner(Initializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public Object apply(Consumer<Collection<ModuleQuery>> injector) {
        List<ModuleQuery> dependencies = Lists.newLinkedList(getDependencies());
        dependencies.addAll(initializer.getDependencies());
        if (dependencies.size() > 0)
            injector.accept(dependencies);

        Object instance = instantiate();
        initializer.init(instance);
        return instance;
    }

    protected abstract Object instantiate();
}