package org.zhupanovdm.microbus.micromod;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.zhupanovdm.microbus.micromod.spawner.InstanceProvider;

import java.util.Collection;
import java.util.function.Consumer;

@Data
@EqualsAndHashCode(of = "id")
@ToString(of = { "id", "type" })
public class Module {
    private final String id;
    private final Class<?> type;
    private final InstanceProvider instanceProvider;

    public Module(String id, Class<?> type, InstanceProvider instanceProvider) {
        this.id = id;
        this.type = type;
        this.instanceProvider = instanceProvider;
    }

    public Object getInstance(Consumer<Collection<ModuleQuery>> injector) {
        return instanceProvider.apply(injector);
    }

}