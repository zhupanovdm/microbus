package org.zhupanovdm.microbus.micromod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.micromod.spawner.ConstructorSpawner;
import org.zhupanovdm.microbus.micromod.spawner.InstanceProvider;
import org.zhupanovdm.microbus.micromod.spawner.MethodSpawner;
import org.zhupanovdm.microbus.micromod.spawner.Spawner;
import org.zhupanovdm.microbus.samples.sample02.With3ArgConstructor;
import org.zhupanovdm.microbus.samples.sample02.WithNoArgConstructor;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
    @DisplayName("Register instance provider")
    void registerInstanceProvider() {
        manager.registerInstanceProvider(new InstanceProvider.Singleton());
        manager.registerInstanceProvider(new InstanceProvider.Factory());

        assertThat(manager.getInstanceProvider(InstanceProvider.Singleton.class), instanceOf(InstanceProvider.Singleton.class));
        assertThat(manager.getInstanceProvider(InstanceProvider.Factory.class), instanceOf(InstanceProvider.Factory.class));
    }

    @Test
    @DisplayName("Register class module")
    void registerClassModule() {
        manager.registerModule("id", With3ArgConstructor.class, InstanceProvider.Singleton.class, Arrays.asList(With3ArgConstructor.class.getDeclaredFields()));
        ArgumentCaptor<Module> moduleArg = ArgumentCaptor.forClass(Module.class);
        verify(registry).register(moduleArg.capture());
        Module module = moduleArg.getValue();

        assertThat(module.getId(), is("id"));
        assertThat(module.getSpawner(), instanceOf(ConstructorSpawner.class));
        assertThat(With3ArgConstructor.class.equals(module.getType()), is(true));
        assertThat(InstanceProvider.Singleton.class.equals(module.getInstanceProviderType()), is(true));
        assertThat(module.getInitFields(), containsInAnyOrder(With3ArgConstructor.class.getDeclaredFields()));
    }

    @Test
    @DisplayName("Register method module")
    void registerMethodModule() throws Exception {
        manager.registerModule("id", WithNoArgConstructor.class.getDeclaredMethod("factoryMethod", Integer.class, String.class, Double.class), "WithNoArgConstructor", InstanceProvider.Singleton.class, Arrays.asList(With3ArgConstructor.class.getDeclaredFields()));

        ArgumentCaptor<Module> moduleArg = ArgumentCaptor.forClass(Module.class);
        verify(registry).register(moduleArg.capture());
        Module module = moduleArg.getValue();

        assertThat(module.getId(), is("id"));
        assertThat(module.getSpawner(), instanceOf(MethodSpawner.class));
        assertThat(With3ArgConstructor.class.equals(module.getType()), is(true));
        assertThat(InstanceProvider.Singleton.class.equals(module.getInstanceProviderType()), is(true));
        assertThat(module.getInitFields(), containsInAnyOrder(With3ArgConstructor.class.getDeclaredFields()));
    }

    @Test
    @DisplayName("Unregister module")
    void unregisterModule() {
        doAnswer(invocation -> {
            ModuleQuery query = invocation.getArgument(0, ModuleQuery.class);
            assertThat(query.getId(), is("id"));
            return new Module(query.getId(), query.getType(), InstanceProvider.Singleton.class);
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
        Set<Module> chain = new LinkedHashSet<>();
        ModuleQuery moduleQuery = ModuleQuery.create("id", With3ArgConstructor.class, true).withChain(chain);
        Spawner spawner = mock(Spawner.class);
        Object instance = new Object();

        doAnswer(invocation -> {
            ModuleQuery registryQuery = invocation.getArgument(0, ModuleQuery.class);
            assertThat(registryQuery.getChain(), sameInstance(chain));

            Module module = new Module(registryQuery.getId(), registryQuery.getType(), InstanceProvider.Singleton.class);
            module.setSpawner(spawner);
            return module;
        }).when(registry).request(eq(moduleQuery));

        doAnswer(invocation -> {
            assertThat(invocation.getArgument(0), sameInstance(chain));
            return instance;
        }).when(spawner).spawn(eq(chain));

        assertThat(manager.resolve(moduleQuery), sameInstance(instance));

        verify(registry).request(eq(moduleQuery));
        verify(spawner).spawn(eq(chain));
    }

}