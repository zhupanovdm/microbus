package org.zhupanovdm.microbus.micromod.spawner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zhupanovdm.microbus.micromod.Module;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

class InstanceProviderTest {

    @Test
    @DisplayName("Singleton instantiation")
    void testSingleton() {
        Module module = new Module("module-id", Object.class, InstanceProvider.Singleton.class);
        Object instance = new Object();

        Function<Module, Object> spawner = mockSpawnerFn(m -> {
            assertThat(m, sameInstance(module));
            return instance;
        });

        InstanceProvider.Singleton singleton = new InstanceProvider.Singleton();
        for (int i = 0; i < 10; i++) {
            assertThat(singleton.apply(module, spawner), sameInstance(instance));
        }

        verify(spawner).apply(eq(module));
    }

    @Test
    @DisplayName("Factory instantiation")
    void testFactory() {
        Module module = new Module("module-id", Object.class, InstanceProvider.Factory.class);

        Function<Module, Object> spawner = mockSpawnerFn(m -> {
            assertThat(m, sameInstance(module));
            return new Object();
        });

        InstanceProvider.Factory factory = new InstanceProvider.Factory();
        Set<Object> result = new HashSet<>();
        for (int i = 0; i < 10; i++)
            assertThat(result.add(factory.apply(module, spawner)), is(true));
        verify(spawner, times(10)).apply(eq(module));
    }

    static Function<Module, Object> mockSpawnerFn(Function<Module, Object> spawner) {
        //noinspection unchecked
        return mock(Function.class, delegatesTo(spawner));
    }
}