package org.zhupanovdm.microbus.micromod;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.zhupanovdm.microbus.micromod.spawner.InstanceProvider;
import org.zhupanovdm.microbus.micromod.spawner.Spawner;

import java.lang.reflect.Field;
import java.util.List;

@Data
@EqualsAndHashCode(of = "id")
@ToString(of = { "id", "type" })
public class Module {
    private final String id;
    private final Class<?> type;
    private final Class<? extends InstanceProvider> instanceProviderType;
    private Spawner spawner;
    private List<Field> initFields;

    public Module(String id, Class<?> type, Class<? extends InstanceProvider> instanceProviderType) {
        this.id = id;
        this.type = type;
        this.instanceProviderType = instanceProviderType;
    }
}