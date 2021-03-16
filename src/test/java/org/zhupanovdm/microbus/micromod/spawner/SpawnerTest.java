package org.zhupanovdm.microbus.micromod.spawner;

import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.micromod.Module;
import org.zhupanovdm.microbus.micromod.ModuleManager;
import org.zhupanovdm.microbus.micromod.ModuleQuery;
import org.zhupanovdm.microbus.samples.sample02.WithNoArgConstructor;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SpawnerTest {
    @Mock
    ModuleManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Spawn an instance")
    void spawn() {
        Module module = new Module("id", List.class, InstanceProvider.Singleton.class);
        ModuleQuery[] dependencies = {
                ModuleQuery.create("dependency-1"),
                ModuleQuery.create("dependency-2"),
                ModuleQuery.create("dependency-3"),
        };
        Spawner spawner = new ListSpawnerImpl(module, manager, dependencies);
        InstanceProvider singleton = spy(new InstanceProvider.Singleton());
        Set<Module> chain = new LinkedHashSet<>();

        doAnswer(invocation -> singleton)
                .when(manager).getInstanceProvider(eq(InstanceProvider.Singleton.class));

        doAnswer(invocation -> {
            ModuleQuery query = invocation.getArgument(0, ModuleQuery.class);
            assertThat(query, isIn(dependencies));
            assertThat(query.getChain(), is(chain));
            assertThat(query.getChain(), contains(module));
            return query;
        }).when(manager).resolve(any(ModuleQuery.class));

        //noinspection unchecked
        List<ModuleQuery> instance = (List<ModuleQuery>) spawner.spawn(chain);
        assertThat(instance, containsInAnyOrder(dependencies));

        verify(singleton).apply(eq(module), any());
        verify(manager).getInstanceProvider(eq(InstanceProvider.Singleton.class));
        verify(manager, times(dependencies.length)).resolve(any(ModuleQuery.class));
    }

    @Test
    @DisplayName("Cyclic dependencies are not allowed")
    void cyclicDependencyFails() {
        Module module = new Module("id", List.class, InstanceProvider.Singleton.class);
        ModuleQuery[] dependencies = { ModuleQuery.create("dependency-1") };
        Spawner spawner = new ListSpawnerImpl(module, manager, dependencies);
        InstanceProvider singleton = new InstanceProvider.Singleton();

        doAnswer(invocation -> singleton)
                .when(manager).getInstanceProvider(eq(InstanceProvider.Singleton.class));

        doAnswer(invocation -> {
            ModuleQuery query = invocation.getArgument(0, ModuleQuery.class);
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> spawner.spawn(query.getChain()));
            assertThat(exception.getMessage(), containsString("Cyclic module dependency"));
            return null;
        }).when(manager).resolve(any(ModuleQuery.class));

        spawner.spawn(new LinkedHashSet<>());
        verify(manager).resolve(any(ModuleQuery.class));
    }

    @Test
    @DisplayName("Init fields of instantiated object")
    void initFields() {
        Module module = new Module("id", WithNoArgConstructor.class, InstanceProvider.Singleton.class);
        module.setInitFields(Arrays.asList(WithNoArgConstructor.class.getDeclaredFields()));
        Spawner spawner = new InitializingSpawnerImpl(module, manager);
        InstanceProvider singleton = spy(new InstanceProvider.Singleton());

        doAnswer(invocation -> singleton)
                .when(manager).getInstanceProvider(eq(InstanceProvider.Singleton.class));

        doAnswer(invocation -> {
            ModuleQuery query = invocation.getArgument(0, ModuleQuery.class);
            assertThat(query.getId(), isIn(new String[] {"i", "s", "d"}));
            assertThat(query.isStrictId(), is(false));
            assertThat(query.getType(), isIn(new Class[] {Integer.class, String.class, Double.class}));

            switch (query.getId()) {
                case "i":
                    return 10;
                case "s":
                    return "a";
                case "d":
                    return 0.3d;
            }

            return null;
        }).when(manager).resolve(any(ModuleQuery.class));

        WithNoArgConstructor instance = (WithNoArgConstructor) spawner.spawn(new LinkedHashSet<>());
        assertThat(instance.getI(), is(10));
        assertThat(instance.getS(), is("a"));
        assertThat(instance.getD(), is(0.3d));

        verify(manager, times(3)).resolve(any(ModuleQuery.class));
    }

    static class InitializingSpawnerImpl extends Spawner {
        public InitializingSpawnerImpl(@NonNull Module module, @NonNull ModuleManager manager) {
            super(module, manager);
        }

        @Override
        protected Object doSpawn(Function<ModuleQuery, Object> injector) {
            return new WithNoArgConstructor();
        }
    }

    static class ListSpawnerImpl extends Spawner {
        private final ModuleQuery[] dependsOn;

        public ListSpawnerImpl(@NonNull Module module, @NonNull ModuleManager manager, ModuleQuery[] dependsOn) {
            super(module, manager);
            this.dependsOn = dependsOn;
        }

        @Override
        protected Object doSpawn(Function<ModuleQuery, Object> injector) {
            return Arrays.stream(dependsOn).map(injector).collect(Collectors.toList());
        }
    }

}