package org.zhupanovdm.microbus.micromod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.micromod.spawner.ClassSpawner;
import org.zhupanovdm.microbus.micromod.spawner.MethodSpawner;
import org.zhupanovdm.microbus.micromod.spawner.SpawnStrategy;
import org.zhupanovdm.microbus.micromod.spawner.Spawner;
import org.zhupanovdm.microbus.samples.sample02.With3ArgConstructor;
import org.zhupanovdm.microbus.samples.sample02.WithNoArgConstructor;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModuleManagerTest {
    @Mock
    ModuleRegistry registry;

    ModuleManager manager;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        manager = new ModuleManager(registry);
    }

    @Test
    @DisplayName("Register class module")
    void registerClassModule() {
        manager.registerModule("id", With3ArgConstructor.class, SpawnStrategy.Singleton.class);
        ArgumentCaptor<Module> moduleArg = ArgumentCaptor.forClass(Module.class);
        verify(registry).register(moduleArg.capture());
        Module module = moduleArg.getValue();

        assertThat(module.getId(), is("id"));
        assertThat(module.getSpawner(), instanceOf(ClassSpawner.class));
        assertThat(With3ArgConstructor.class.equals(module.getType()), is(true));
        assertThat(SpawnStrategy.Singleton.class.equals(module.getSpawnStrategy()), is(true));
    }

    @Test
    @DisplayName("Register method module")
    void registerMethodModule() throws Exception {
        manager.registerModule("id", WithNoArgConstructor.class.getDeclaredMethod("factoryMethod", Integer.class, String.class, Double.class), "WithNoArgConstructor", SpawnStrategy.Singleton.class);

        ArgumentCaptor<Module> moduleArg = ArgumentCaptor.forClass(Module.class);
        verify(registry).register(moduleArg.capture());
        Module module = moduleArg.getValue();

        assertThat(module.getId(), is("id"));
        assertThat(module.getSpawner(), instanceOf(MethodSpawner.class));
        assertThat(With3ArgConstructor.class.equals(module.getType()), is(true));
        assertThat(SpawnStrategy.Singleton.class.equals(module.getSpawnStrategy()), is(true));
    }

    @Test
    @DisplayName("Unregister module")
    void unregisterModule() {
        doAnswer(invocation -> {
            ModuleQuery query = invocation.getArgument(0, ModuleQuery.class);
            assertThat(query.getId(), is("id"));
            return new Module(query.getId(), query.getType(), SpawnStrategy.Singleton.class);
        }).when(registry).request(any(ModuleQuery.class));

        manager.unregisterModule("id");
        ArgumentCaptor<Module> moduleArg = ArgumentCaptor.forClass(Module.class);

        verify(registry).unregister(moduleArg.capture());
        Module module = moduleArg.getValue();
        assertThat(module.getId(), is("id"));
    }

    @Test
    @DisplayName("Resolve dependency")
    void resolve() {
        ModuleQuery moduleQuery = ModuleQuery.of("id", With3ArgConstructor.class, true);
        Spawner spawner = mock(Spawner.class);
        Object instance = new Object();

        doAnswer(invocation -> {
            ModuleQuery registryQuery = invocation.getArgument(0, ModuleQuery.class);
            Module module = new Module(registryQuery.getId(), registryQuery.getType(), SpawnStrategy.Singleton.class);
            module.setSpawner(spawner);
            return module;
        }).when(registry).request(eq(moduleQuery));

        doAnswer(invocation -> instance).when(spawner).get();

        assertThat(manager.resolve(moduleQuery), sameInstance(instance));

        verify(registry).request(eq(moduleQuery));
        verify(spawner).get();
    }

}