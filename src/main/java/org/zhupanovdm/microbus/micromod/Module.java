package org.zhupanovdm.microbus.micromod;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.zhupanovdm.microbus.micromod.spawner.Initializer;
import org.zhupanovdm.microbus.micromod.spawner.SpawnStrategy;
import org.zhupanovdm.microbus.micromod.spawner.Spawner;

@Data
@EqualsAndHashCode(of = "id")
@ToString(of = { "id", "type" })
public class Module {
    private final String id;
    private final Class<?> type;

    private Spawner spawner;
    private final Class<? extends SpawnStrategy> spawnStrategy;
    private Initializer initializer;

    public Module(String id, Class<?> type, Class<? extends SpawnStrategy> spawnStrategy) {
        this.id = id;
        this.type = type;
        this.spawnStrategy = spawnStrategy;
    }

}